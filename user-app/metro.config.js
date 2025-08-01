const { getDefaultConfig } = require('@expo/metro-config');

// Get the default Metro configuration
const defaultConfig = getDefaultConfig(__dirname);

// Configure the SVG transformer
defaultConfig.transformer.babelTransformerPath = require.resolve('react-native-svg-transformer');

// Modify asset and source extensions to handle SVGs
defaultConfig.resolver.assetExts = defaultConfig.resolver.assetExts.filter(ext => ext !== 'svg');
defaultConfig.resolver.sourceExts.push('svg');

// Keep existing resolver settings
defaultConfig.resolver.sourceExts.push('cjs');
defaultConfig.resolver.unstable_enablePackageExports = false;

module.exports = defaultConfig;
