// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const { themes } = require('prism-react-renderer');
const lightCodeTheme = themes.github;
const darkCodeTheme = themes.dracula;

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'ZUtil',
  tagline: '追求更快更全的 Java 工具类',
  url: 'https://duanluan.github.io',
  baseUrl: '/zutil/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'duanluan', // Usually your GitHub org/user name.
  projectName: 'zutil', // Usually your repo name.
  // deploymentBranch: 'gh-pages',
  trailingSlash: false,

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'zh-Hans',
    locales: ['zh-Hans'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl: 'https://github.com/duanluan/zutil/tree/main/docs',
        },
        blog: {
          showReadingTime: true,
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl: 'https://github.com/duanluan/zutil/tree/main/docs',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
  /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'ZUtil',
        logo: {
          alt: 'My Site Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'doc',
            docId: 'intro',
            position: 'left',
            label: '教程',
          },
          {
            to: '/blog',
            label: '博客',
            position: 'left'
          },
          {
            type: 'html',
            value: '<a href="https://github.com/duanluan/zutil" target="_blank" rel="noopener noreferrer" class="navbar__item navbar__link header-github-link" aria-label="GitHub repository"></a>',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: '教程',
                to: '/docs/intro',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'QQ 群',
                href: 'https://jq.qq.com/?_wv=1027&k=Jzpzg0lc',
              },
              {
                label: 'GitHub Discussions',
                href: 'https://github.com/duanluan/zutil/discussions',
              },
              {
                label: 'SegmentFault 问答',
                href: 'https://segmentfault.com/search?q=zutil&type=qa',
              },
              {
                label: '开源中国问答',
                href: 'https://www.oschina.net/search?scope=bbs&q=zutil',
              },
              {
                label: 'CSDN 问答',
                href: 'https://so.csdn.net/so/search?q=zutil&t=ask',
              }
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'Blog',
                to: '/blog',
              },
              {
                label: 'GitHub',
                href: 'https://github.com/duanluan/zutil',
              },
              {
                label: 'Gitee',
                href: 'https://gitee.com/duanluan/zutil',
              }
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} ZUtil, Inc. Built with duanluan.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

module.exports = config;
