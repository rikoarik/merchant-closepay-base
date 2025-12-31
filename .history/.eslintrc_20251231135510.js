/**
 * ESLint Configuration
 * Extends React Native ESLint config with additional rules
 * Integrated with Prettier to avoid formatting conflicts
 */

module.exports = {
  root: true,
  extends: [
    '@react-native',
    'prettier', // Must be last to override other configs
  ],
  plugins: ['prettier'],
  rules: {
    // Prettier integration
    'prettier/prettier': 'warn',
    // Naming consistency rules
    '@typescript-eslint/naming-convention': [
      'warn',
      {
        // Interfaces and types: PascalCase
        selector: ['typeLike', 'interface'],
        format: ['PascalCase'],
      },
      {
        // Variables and functions: camelCase
        selector: ['variable', 'function'],
        format: ['camelCase', 'PascalCase'], // Allow PascalCase for React components
      },
      {
        // Constants: UPPER_CASE or PascalCase
        selector: 'variable',
        modifiers: ['const'],
        format: ['UPPER_CASE', 'PascalCase', 'camelCase'], // Allow all for flexibility
      },
      {
        // Classes: PascalCase
        selector: 'class',
        format: ['PascalCase'],
      },
      {
        // Enums: PascalCase
        selector: 'enum',
        format: ['PascalCase'],
      },
    ],
    // Prefer consistent error variable naming
    'prefer-const': 'warn',
    // No unused variables
    '@typescript-eslint/no-unused-vars': [
      'warn',
      {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_',
      },
    ],
  },
};

