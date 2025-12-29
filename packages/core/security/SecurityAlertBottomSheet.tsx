/**
 * Security Alert Bottom Sheet
 * Non-dismissible bottom sheet untuk security threats
 * Hanya bisa ditutup dengan tombol "Close App"
 */
import React from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    Platform,
    BackHandler,
} from 'react-native';
import { BottomSheet } from '@core/config/components/ui/BottomSheet';
import { useTheme } from '@core/theme';
import {
    scale,
    moderateScale,
    moderateVerticalScale,
    getResponsiveFontSize,
    getMinTouchTarget,
} from '@core/config/utils/responsive';
import { getFontFamily } from '@core/config/utils/fonts';

// Helper untuk mendapatkan font family dengan fallback yang aman
const getFont = (variant: 'regular' | 'medium' | 'semiBold' | 'bold'): string => {
    try {
        return getFontFamily(variant, false);
    } catch (error) {
        return getFontFamily(variant, true);
    }
};

interface SecurityAlertBottomSheetProps {
    visible: boolean;
    threatType: string;
    message: string;
    onCloseApp: () => void;
}

export const SecurityAlertBottomSheet: React.FC<SecurityAlertBottomSheetProps> = ({
    visible,
    threatType,
    message,
    onCloseApp,
}) => {
    const { colors } = useTheme();

    return (
        <BottomSheet
            visible={visible}
            onClose={() => { }} // Empty - tidak bisa ditutup kecuali lewat button
            snapPoints={[40]} // 40% screen height
            initialSnapPoint={0}
            enablePanDownToClose={false} // Disable drag to close
            disableClose={true} // Disable backdrop press & back button
        >
            <View style={styles.container}>
                {/* Icon Section */}
                <View style={styles.iconContainer}>
                    <View style={[styles.iconCircle, { backgroundColor: colors.errorLight }]}>
                        <Text style={styles.iconText}>⚠️</Text>
                    </View>
                </View>

                {/* Content Section */}
                <View style={styles.contentContainer}>
                    <Text style={[styles.title, { color: colors.error }]}>
                        Security Alert
                    </Text>
                    <Text style={[styles.threatType, { color: colors.text }]}>
                        {threatType}
                    </Text>
                    <Text style={[styles.message, { color: colors.textSecondary }]}>
                        {message}
                    </Text>
                    <Text style={[styles.info, { color: colors.textTertiary }]}>
                        This app cannot run on compromised devices. Please use a secure device.
                    </Text>
                </View>

                {/* Button Section */}
                <TouchableOpacity
                    style={[styles.button, { backgroundColor: colors.error }]}
                    onPress={onCloseApp}
                    activeOpacity={0.8}
                >
                    <Text style={[styles.buttonText, { color: colors.surface }]}>
                        Close App
                    </Text>
                </TouchableOpacity>
            </View>
        </BottomSheet>
    );
};

const styles = StyleSheet.create({
    container: {
        paddingHorizontal: scale(20),
    },
    iconContainer: {
        alignItems: 'center',
        marginTop: moderateVerticalScale(12),
        marginBottom: moderateVerticalScale(16),
    },
    iconCircle: {
        width: scale(64),
        height: scale(64),
        borderRadius: scale(32),
        justifyContent: 'center',
        alignItems: 'center',
    },
    iconText: {
        fontSize: moderateScale(36),
    },
    contentContainer: {
        marginBottom: moderateVerticalScale(24),
    },
    title: {
        fontSize: moderateScale(24),
        fontFamily: getFont('bold'),
        textAlign: 'center',
        marginBottom: moderateVerticalScale(8),
    },
    threatType: {
        fontSize: moderateScale(18),
        fontFamily: getFont('semiBold'),
        textAlign: 'center',
        marginBottom: moderateVerticalScale(12),
    },
    message: {
        fontSize: getResponsiveFontSize('medium'),
        fontFamily: getFont('regular'),
        textAlign: 'center',
        lineHeight: moderateVerticalScale(22),
        marginBottom: moderateVerticalScale(12),
    },
    info: {
        fontSize: getResponsiveFontSize('small'),
        fontFamily: getFont('regular'),
        textAlign: 'center',
        lineHeight: moderateVerticalScale(18),
        fontStyle: 'italic',
    },
    button: {
        borderRadius: scale(12),
        paddingVertical: moderateVerticalScale(14),
        marginBottom: moderateVerticalScale(16),
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: getMinTouchTarget(),
        ...Platform.select({
            ios: {
                shadowColor: '#000',
                shadowOffset: { width: 0, height: 4 },
                shadowOpacity: 0.3,
                shadowRadius: 8,
            },
            android: {
                elevation: 4,
            },
        }),
    },
    buttonText: {
        fontSize: getResponsiveFontSize('large'),
        fontFamily: getFont('semiBold'),
        letterSpacing: 0.5,
    },
});
