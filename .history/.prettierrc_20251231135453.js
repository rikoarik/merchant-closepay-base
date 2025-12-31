/**
 * Prettier Configuration
 * Code formatting rules for consistent code style
 */

module.exports = {
  // Basic formatting
  semi: true,
  singleQuote: true,
  quoteProps: 'as-needed',
  trailingComma: 'es5',
  tabWidth: 2,
  useTabs: false,
  printWidth: 100,
  
  // JSX/TSX specific
  jsxSingleQuote: false,
  bracketSpacing: true,
  bracketSameLine: false,
  arrowParens: 'always',
  
  // React Native specific
  endOfLine: 'lf',
  
  // Plugin support
  plugins: [],
};

