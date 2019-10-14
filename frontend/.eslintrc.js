module.exports = {
  "root": true,
  "env": {
    "node": true
  },
  "extends": [
    "plugin:vue/essential",
    "eslint:recommended",
    "@vue/typescript"
  ],
  "rules": {
    "indent": ["warn", 2]
  },
  "parserOptions": {
    "parser": "@typescript-eslint/parser"
  }
};
