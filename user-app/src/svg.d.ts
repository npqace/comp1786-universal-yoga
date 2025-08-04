/**
 * @file svg.d.ts
 * @description TypeScript declaration file for importing SVG files as React components.
 * This allows using SVGs like: `import Logo from './logo.svg';`
 */
declare module '*.svg' {
  import React from 'react';
  import { SvgProps } from 'react-native-svg';
  const content: React.FC<SvgProps>;
  export default content;
}