/**
 * ProfileScreen Component
 * Screen untuk menampilkan menu profile
 * Responsive untuk semua device termasuk EDC
 */
import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Image,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import {
  Profile,
  DocumentText,
  Grid,
  Sun1,
  Document,
  LogoutCurve,
  Menu,
} from 'iconsax-react-nativejs';
import { useTheme } from '@core/theme';
import { useTranslation } from '@core/i18n';
import { useAuthStore, useAuth } from '@core/auth';
import {
  scale,
  moderateScale,
  moderateVerticalScale,
  getHorizontalPadding,
  getVerticalPadding,
  getMinTouchTarget,
  getResponsiveFontSize,
  getIconSize,
  FontFamily,
} from '@core/config';
import { ScreenHeader } from '../../config/components/ui/ScreenHeader';
import { ArrowLeft2, LanguageSquare } from 'iconsax-react-nativejs';

interface MenuItem {
  id: string;
  label: string;
  icon: React.ReactNode;
  onPress: () => void;
}

export const ProfileScreen: React.FC = () => {
  const navigation = useNavigation();
  const { colors } = useTheme();
  const { t } = useTranslation();
  const insets = useSafeAreaInsets();
  const logout = useAuthStore((state: any) => state.logout);
  const { user } = useAuth();

  const menuItems: MenuItem[] = [
    {
      id: 'edit-profile',
      label: t('profile.editProfile'),
      icon: <Profile size={getIconSize('medium')} color={colors.text} variant="Outline" />,
      onPress: () => {
        // @ts-ignore - navigation type akan di-setup nanti
        navigation.navigate('EditProfile');
      },
    },
    {
      id: 'language',
      label: t('profile.language'),
      icon: <LanguageSquare size={getIconSize('medium')} color={colors.text} variant="Outline" />,
      onPress: () => {
        // @ts-ignore - navigation type akan di-setup nanti
        navigation.navigate('LanguageSelection');
      },
    },
    {
      id: 'quick-menu',
      label: t('profile.quickMenu'),
      icon: <Menu size={getIconSize('medium')} color={colors.text} variant="Outline" />,
      onPress: () => {
        // @ts-ignore - navigation type akan di-setup nanti
        navigation.navigate('QuickMenuSettings');
      },
    },
    {
      id: 'theme',
      label: t('profile.theme'),
      icon: <Sun1 size={getIconSize('medium')} color={colors.text} variant="Outline" />,
      onPress: () => {
        // @ts-ignore - navigation type akan di-setup nanti
        navigation.navigate('ThemeSettings');
      },
    },
    {
      id: 'terms',
      label: t('profile.termsAndConditions'),
      icon: <Document size={getIconSize('medium')} color={colors.text} variant="Outline" />,
      onPress: () => {
        // TODO: Navigate to terms and conditions
        console.log('Terms and conditions');
      },
    },
    {
      id: 'privacy',
      label: t('profile.privacyPolicy'),
      icon: <DocumentText size={getIconSize('medium')} color={colors.text} variant="Outline" />,
      onPress: () => {
        // TODO: Navigate to privacy policy
        console.log('Privacy policy');
      },
    },
    {
      id: 'logout',
      label: t('profile.logout'),
      icon: (
        <View style={{ transform: [{ rotate: '180deg' }] }}>
          <LogoutCurve size={getIconSize('medium')} color={colors.error} variant="Outline" />
        </View>
      ),
      onPress: () => {
        logout();
      },
    },
  ];

  const [showLogoutDialog, setShowLogoutDialog] = React.useState(false);

  const handleLogout = () => {
    setShowLogoutDialog(true);
  };

  const confirmLogout = () => {
    setShowLogoutDialog(false);
    logout();
  };

  return (
    <SafeAreaView
      style={[
        styles.container,
        {
          backgroundColor: colors.background,
        },
      ]}
    >
      {/* Header */}

      <ScreenHeader title={t('profile.title')} />

      {/* Menu List */}
      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={[
          styles.scrollContent,
          { paddingHorizontal: getHorizontalPadding() },
        ]}
        showsVerticalScrollIndicator={false}
      >
        {/* Profile Card */}
        <View
          style={[
            styles.profileCard,
            {
              backgroundColor: colors.surface,
              borderColor: colors.border,
            },
          ]}
        >
          <View style={styles.profileCardContent}>
            {/* Profile Picture */}
            <View style={styles.profilePictureContainer}>
              <View
                style={[
                  styles.profilePicturePlaceholder,
                  { backgroundColor: colors.primaryLight },
                ]}
              >
                {user?.name ? (
                  <Text
                    style={[
                      styles.profilePictureText,
                      { color: colors.primary },
                      { fontSize: getResponsiveFontSize('large') },
                    ]}
                  >
                    {user.name
                      .split(' ')
                      .map((n) => n[0])
                      .join('')
                      .toUpperCase()
                      .slice(0, 2)}
                  </Text>
                ) : (
                  <Profile
                    size={getIconSize('large')}
                    color={colors.primary}
                    variant="Bold"
                  />
                )}
              </View>
            </View>

            {/* User Info */}
            <View style={styles.profileInfo}>
              <Text
                style={[
                  styles.profileName,
                  {
                    color: colors.text,
                    fontSize: getResponsiveFontSize('large'),
                  },
                ]}
              >
                {user?.name || t('profile.name')}
              </Text>
              <Text
                style={[
                  styles.profileEmail,
                  {
                    color: colors.textSecondary,
                    fontSize: getResponsiveFontSize('small'),
                  },
                ]}
              >
                {user?.email || 'email mungkin'}
              </Text>
            </View>
          </View>
        </View>

        {menuItems.map((item) => (
          <TouchableOpacity
            key={item.id}
            style={[
              styles.menuItem,
              {
                backgroundColor: colors.surface,
                borderColor: colors.border,
                minHeight: getMinTouchTarget(),
              },
            ]}
            onPress={item.id === 'logout' ? handleLogout : item.onPress}
            activeOpacity={0.7}
          >
            <View style={styles.menuItemLeft}>
              <View style={styles.iconContainer}>{item.icon}</View>
              <Text
                style={[
                  styles.menuItemLabel,
                  {
                    color: colors.text,
                    fontSize: getResponsiveFontSize('medium'),
                  },
                ]}
              >
                {item.label}
              </Text>
            </View>
            <View style={{ transform: [{ rotate: '180deg' }] }}>
              <ArrowLeft2
                size={getIconSize('small')}
                color={colors.textTertiary}
                variant="Outline"
              />
            </View>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {/* Logout Confirmation Dialog */}
      {showLogoutDialog && (
        <View
          style={[
            StyleSheet.absoluteFill,
            {
              backgroundColor: 'rgba(0,0,0,0.5)',
              justifyContent: 'center',
              alignItems: 'center',
              zIndex: 1000,
            },
          ]}
        >
          <View
            style={{
              backgroundColor: colors.surface,
              borderRadius: scale(16),
              padding: scale(24),
              width: '80%',
              maxWidth: 400,
            }}
          >
            <Text
              style={{
                fontFamily: FontFamily.monasans.bold,
                fontSize: getResponsiveFontSize('large'),
                color: colors.text,
                marginBottom: moderateVerticalScale(8),
                textAlign: 'center',
              }}
            >
              {t('profile.logout')}
            </Text>
            <Text
              style={{
                fontFamily: FontFamily.monasans.regular,
                fontSize: getResponsiveFontSize('medium'),
                color: colors.textSecondary,
                marginBottom: moderateVerticalScale(24),
                textAlign: 'center',
              }}
            >
              {t('profile.logoutConfirmation')}
            </Text>
            <View style={{ flexDirection: 'row', gap: scale(12) }}>
              <TouchableOpacity
                style={{
                  flex: 1,
                  paddingVertical: moderateVerticalScale(12),
                  borderRadius: scale(12),
                  borderWidth: 1,
                  borderColor: colors.border,
                  alignItems: 'center',
                }}
                onPress={() => setShowLogoutDialog(false)}
              >
                <Text
                  style={{
                    fontFamily: FontFamily.monasans.medium,
                    color: colors.text,
                    fontSize: getResponsiveFontSize('medium'),
                  }}
                >
                  {t('common.cancel')}
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={{
                  flex: 1,
                  paddingVertical: moderateVerticalScale(12),
                  borderRadius: scale(12),
                  backgroundColor: colors.error,
                  alignItems: 'center',
                }}
                onPress={confirmLogout}
              >
                <Text
                  style={{
                    fontFamily: FontFamily.monasans.medium,
                    color: colors.surface,
                    fontSize: getResponsiveFontSize('medium'),
                  }}
                >
                  {t('profile.logout')}
                </Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  backButton: {
    padding: moderateVerticalScale(4),
    minWidth: getMinTouchTarget(),
    minHeight: getMinTouchTarget(),
    justifyContent: 'center',
    alignItems: 'flex-start',
  },
  title: {
    fontFamily: FontFamily.monasans.bold,
    flex: 1,
    textAlign: 'center',
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    paddingTop: moderateVerticalScale(16),
    paddingBottom: moderateVerticalScale(32),
  },
  profileCard: {
    borderRadius: scale(12),
    borderWidth: 1,
    marginBottom: moderateVerticalScale(16),
    padding: scale(16),
  },
  profileCardContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  profilePictureContainer: {
    marginRight: scale(16),
  },
  profilePicturePlaceholder: {
    width: scale(64),
    height: scale(64),
    borderRadius: scale(32),
    justifyContent: 'center',
    alignItems: 'center',
  },
  profilePictureText: {
    fontFamily: FontFamily.monasans.bold,
  },
  profileInfo: {
    flex: 1,
  },
  profileName: {
    fontFamily: FontFamily.monasans.bold,
    marginBottom: moderateVerticalScale(4),
  },
  profileEmail: {
    fontFamily: FontFamily.monasans.regular,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: scale(16),
    paddingVertical: moderateVerticalScale(16),
    borderRadius: scale(12),
    borderWidth: 1,
    marginBottom: moderateVerticalScale(12),
  },
  menuItemLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  iconContainer: {
    marginRight: scale(12),
  },
  menuItemLabel: {
    fontFamily: FontFamily.monasans.regular,
    flex: 1,
  },
});

