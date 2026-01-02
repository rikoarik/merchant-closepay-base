import UIKit
import React
import Photos

@objc(ImagePickerModule)
class ImagePickerModule: NSObject {
    private var promise: RCTPromiseResolveBlock?
    private var reject: RCTPromiseRejectBlock?
    private var currentViewController: UIViewController?
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc
    func pickImage(_ options: NSDictionary, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        self.promise = resolver
        self.reject = rejecter
        
        DispatchQueue.main.async {
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let rootViewController = windowScene.windows.first?.rootViewController else {
                rejecter("NO_VIEW_CONTROLLER", "No root view controller found", nil)
                return
            }
            
            // Find the topmost view controller
            var topViewController = rootViewController
            while let presented = topViewController.presentedViewController {
                topViewController = presented
            }
            
            self.currentViewController = topViewController
            
            let imagePicker = UIImagePickerController()
            imagePicker.sourceType = .photoLibrary
            imagePicker.delegate = self
            imagePicker.allowsEditing = false
            
            topViewController.present(imagePicker, animated: true)
        }
    }
    
    @objc
    func takePicture(_ options: NSDictionary, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        self.promise = resolver
        self.reject = rejecter
        
        DispatchQueue.main.async {
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let rootViewController = windowScene.windows.first?.rootViewController else {
                rejecter("NO_VIEW_CONTROLLER", "No root view controller found", nil)
                return
            }
            
            // Find the topmost view controller
            var topViewController = rootViewController
            while let presented = topViewController.presentedViewController {
                topViewController = presented
            }
            
            self.currentViewController = topViewController
            
            let imagePicker = UIImagePickerController()
            imagePicker.sourceType = .camera
            imagePicker.delegate = self
            imagePicker.allowsEditing = false
            
            topViewController.present(imagePicker, animated: true)
        }
    }
}

extension ImagePickerModule: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        picker.dismiss(animated: true) {
            guard let image = info[.originalImage] as? UIImage else {
                self.reject?("NO_IMAGE", "Failed to get image", nil)
                self.cleanup()
                return
            }
            
            // Show crop view controller
            self.showCropViewController(image: image)
        }
    }
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true) {
            self.reject?("CANCELLED", "User cancelled image selection", nil)
            self.cleanup()
        }
    }
    
    private func showCropViewController(image: UIImage) {
        guard let viewController = currentViewController else {
            reject?("NO_VIEW_CONTROLLER", "No view controller found", nil)
            cleanup()
            return
        }
        
        let cropViewController = CropViewController(image: image)
        cropViewController.delegate = self
        cropViewController.modalPresentationStyle = .fullScreen
        viewController.present(cropViewController, animated: true)
    }
    
    private func cleanup() {
        promise = nil
        reject = nil
        currentViewController = nil
    }
}

extension ImagePickerModule: CropViewControllerDelegate {
    func cropViewController(_ controller: CropViewController, didCropImage image: UIImage) {
        controller.dismiss(animated: true) {
            // Save cropped image to temporary directory
            guard let imageData = image.jpegData(compressionQuality: 0.8) else {
                self.reject?("SAVE_ERROR", "Failed to save cropped image", nil)
                self.cleanup()
                return
            }
            
            let tempDir = FileManager.default.temporaryDirectory
            let fileName = "cropped_\(UUID().uuidString).jpg"
            let fileURL = tempDir.appendingPathComponent(fileName)
            
            do {
                try imageData.write(to: fileURL)
                let result: [String: Any] = [
                    "uri": fileURL.absoluteString
                ]
                self.promise?(result)
            } catch {
                self.reject?("SAVE_ERROR", "Failed to save cropped image: \(error.localizedDescription)", error)
            }
            
            self.cleanup()
        }
    }
    
    func cropViewControllerDidCancel(_ controller: CropViewController) {
        controller.dismiss(animated: true) {
            self.reject?("CANCELLED", "User cancelled crop", nil)
            self.cleanup()
        }
    }
}

// Simple Crop View Controller with 1:1 aspect ratio
class CropViewController: UIViewController {
    private let image: UIImage
    private let imageView = UIImageView()
    private let cropOverlay = UIView()
    private let cropFrame = UIView()
    private var initialFrame: CGRect = .zero
    private var cropRect: CGRect = .zero
    
    weak var delegate: CropViewControllerDelegate?
    
    init(image: UIImage) {
        self.image = image
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    private func setupUI() {
        view.backgroundColor = .black
        
        // Image view
        imageView.image = image
        imageView.contentMode = .scaleAspectFit
        imageView.isUserInteractionEnabled = true
        view.addSubview(imageView)
        
        // Crop overlay (dark overlay)
        cropOverlay.backgroundColor = UIColor.black.withAlphaComponent(0.7)
        view.addSubview(cropOverlay)
        
        // Crop frame (transparent area)
        cropFrame.layer.borderColor = UIColor.white.cgColor
        cropFrame.layer.borderWidth = 2
        cropFrame.backgroundColor = .clear
        view.addSubview(cropFrame)
        
        // Navigation bar
        let navBar = UINavigationBar()
        navBar.barStyle = .black
        navBar.translucent = false
        view.addSubview(navBar)
        
        let navItem = UINavigationItem(title: "Crop Image")
        let cancelButton = UIBarButtonItem(barButtonSystemItem: .cancel, target: self, action: #selector(cancelTapped))
        let doneButton = UIBarButtonItem(barButtonSystemItem: .done, target: self, action: #selector(doneTapped))
        navItem.leftBarButtonItem = cancelButton
        navItem.rightBarButtonItem = doneButton
        navBar.setItems([navItem], animated: false)
        
        // Layout
        imageView.translatesAutoresizingMaskIntoConstraints = false
        cropOverlay.translatesAutoresizingMaskIntoConstraints = false
        cropFrame.translatesAutoresizingMaskIntoConstraints = false
        navBar.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            navBar.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            navBar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            navBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            
            imageView.topAnchor.constraint(equalTo: navBar.bottomAnchor),
            imageView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            imageView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            imageView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            
            cropOverlay.topAnchor.constraint(equalTo: imageView.topAnchor),
            cropOverlay.leadingAnchor.constraint(equalTo: imageView.leadingAnchor),
            cropOverlay.trailingAnchor.constraint(equalTo: imageView.trailingAnchor),
            cropOverlay.bottomAnchor.constraint(equalTo: imageView.bottomAnchor),
            
            cropFrame.centerXAnchor.constraint(equalTo: imageView.centerXAnchor),
            cropFrame.centerYAnchor.constraint(equalTo: imageView.centerYAnchor),
            cropFrame.widthAnchor.constraint(equalTo: cropFrame.heightAnchor),
            cropFrame.widthAnchor.constraint(lessThanOrEqualTo: imageView.widthAnchor, multiplier: 0.9),
            cropFrame.heightAnchor.constraint(lessThanOrEqualTo: imageView.heightAnchor, multiplier: 0.9),
        ])
        
        // Add pan gesture for moving crop frame
        let panGesture = UIPanGestureRecognizer(target: self, action: #selector(handlePan(_:)))
        cropFrame.addGestureRecognizer(panGesture)
        
        // Add pinch gesture for resizing crop frame
        let pinchGesture = UIPinchGestureRecognizer(target: self, action: #selector(handlePinch(_:)))
        cropFrame.addGestureRecognizer(pinchGesture)
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        updateCropOverlay()
    }
    
    private func updateCropOverlay() {
        let cropFrameRect = cropFrame.frame
        let maskLayer = CAShapeLayer()
        let path = UIBezierPath(rect: cropOverlay.bounds)
        let cropPath = UIBezierPath(rect: cropFrameRect)
        path.append(cropPath.reversing())
        maskLayer.path = path.cgPath
        cropOverlay.layer.mask = maskLayer
    }
    
    @objc private func handlePan(_ gesture: UIPanGestureRecognizer) {
        let translation = gesture.translation(in: view)
        
        if gesture.state == .began {
            initialFrame = cropFrame.frame
        }
        
        var newFrame = initialFrame
        newFrame.origin.x += translation.x
        newFrame.origin.y += translation.y
        
        // Keep crop frame within image view bounds
        newFrame.origin.x = max(imageView.frame.minX, min(newFrame.origin.x, imageView.frame.maxX - newFrame.width))
        newFrame.origin.y = max(imageView.frame.minY, min(newFrame.origin.y, imageView.frame.maxY - newFrame.height))
        
        cropFrame.frame = newFrame
        updateCropOverlay()
    }
    
    @objc private func handlePinch(_ gesture: UIPinchGestureRecognizer) {
        if gesture.state == .began {
            initialFrame = cropFrame.frame
        }
        
        let scale = gesture.scale
        let newWidth = initialFrame.width * scale
        let newHeight = initialFrame.height * scale
        
        // Maintain 1:1 aspect ratio
        let minSize: CGFloat = 100
        let maxSize = min(imageView.frame.width, imageView.frame.height) * 0.9
        let clampedSize = max(minSize, min(maxSize, newWidth))
        
        var newFrame = initialFrame
        newFrame.size.width = clampedSize
        newFrame.size.height = clampedSize
        newFrame.origin.x = initialFrame.midX - clampedSize / 2
        newFrame.origin.y = initialFrame.midY - clampedSize / 2
        
        // Keep crop frame within image view bounds
        newFrame.origin.x = max(imageView.frame.minX, min(newFrame.origin.x, imageView.frame.maxX - newFrame.width))
        newFrame.origin.y = max(imageView.frame.minY, min(newFrame.origin.y, imageView.frame.maxY - newFrame.height))
        
        cropFrame.frame = newFrame
        updateCropOverlay()
    }
    
    @objc private func doneTapped() {
        // Crop the image
        let cropRect = convertCropRectToImageCoordinates()
        guard let croppedImage = cropImage(image: image, toRect: cropRect) else {
            delegate?.cropViewControllerDidCancel(self)
            return
        }
        
        delegate?.cropViewController(self, didCropImage: croppedImage)
    }
    
    @objc private func cancelTapped() {
        delegate?.cropViewControllerDidCancel(self)
    }
    
    private func convertCropRectToImageCoordinates() -> CGRect {
        let imageViewSize = imageView.bounds.size
        let imageSize = image.size
        
        let scaleX = imageSize.width / imageViewSize.width
        let scaleY = imageSize.height / imageViewSize.height
        let scale = max(scaleX, scaleY)
        
        let imageViewFrame = imageView.frame
        let cropFrameInImageView = cropFrame.frame
        
        // Convert crop frame to image coordinates
        let x = (cropFrameInImageView.origin.x - imageViewFrame.origin.x) * scale
        let y = (cropFrameInImageView.origin.y - imageViewFrame.origin.y) * scale
        let width = cropFrameInImageView.width * scale
        let height = cropFrameInImageView.height * scale
        
        return CGRect(x: x, y: y, width: width, height: height)
    }
    
    private func cropImage(image: UIImage, toRect rect: CGRect) -> UIImage? {
        guard let cgImage = image.cgImage?.cropping(to: rect) else {
            return nil
        }
        return UIImage(cgImage: cgImage, scale: image.scale, orientation: image.imageOrientation)
    }
}

protocol CropViewControllerDelegate: AnyObject {
    func cropViewController(_ controller: CropViewController, didCropImage image: UIImage)
    func cropViewControllerDidCancel(_ controller: CropViewController)
}
