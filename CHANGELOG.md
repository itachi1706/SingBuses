# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [5.2.3] - 2024-07-08
### :sparkles: New Features
- [`e431530`](https://github.com/itachi1706/SingBuses/commit/e431530a107b6a813303f1eafd5896c69610f204) - SGBUSAND-279 Migrated to Kotlin DSL + Version Catalog *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`543910e`](https://github.com/itachi1706/SingBuses/commit/543910ee40af87cc871f4199d1cbb84220b9b058) - SGBUSAND-290 Migrated to use the new recommended way to protect Google API Keys *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`057ce6d`](https://github.com/itachi1706/SingBuses/commit/057ce6dd6f4881eb9ea2813ca48005902176f396) - SGBUSAND-280 Disable NTU Buses *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`15b944a`](https://github.com/itachi1706/SingBuses/commit/15b944a177b06d3637aa21d8ebae6ac68fe5262d) - SGBUSAND-282 Upgraded to SDK 34 *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`efbb5ee`](https://github.com/itachi1706/SingBuses/commit/efbb5ee116e8afbf500ccd87c5f2d2271d079799) - Bump Min SDK up to 21 *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`e1b4491`](https://github.com/itachi1706/SingBuses/commit/e1b4491e22beb70c7c94e0ebc8efcb2d95b3824d) - SGBUSAND-86 Replaced all PNG files with smaller WebP files *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`b5a5560`](https://github.com/itachi1706/SingBuses/commit/b5a5560d4b2d01925ea47204ea912d273da71aee) - Bump Min SDK up to 23 (Android 6) *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`923a1ae`](https://github.com/itachi1706/SingBuses/commit/923a1aeb173039e8683598bd9a371d6a584b20f8) - SGBUSAND-241 Replaced Process Dialog with new Foreground Services *(commit by [@itachi1706](https://github.com/itachi1706))*

### :bug: Bug Fixes
- [`fe0bc39`](https://github.com/itachi1706/SingBuses/commit/fe0bc39e40c007e392e79eab025df427fbde0762) - SGBUSAND-269 Close all DB connections when done *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`ea13011`](https://github.com/itachi1706/SingBuses/commit/ea13011b97e2f6c4109e091b2837b7121edff2d4) - Properly handle JSON Strings *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`d5b7950`](https://github.com/itachi1706/SingBuses/commit/d5b7950b41d553c0f599dffd3ca60b9fadf8d6ac) - Refactored Dynamic Shortcuts *(commit by [@itachi1706](https://github.com/itachi1706))*

### :recycle: Refactors
- [`45fe360`](https://github.com/itachi1706/SingBuses/commit/45fe360d8a268bea53cffbfa38e51c45dea44a4a) - **i18n**: Added some accessibility strings *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`c9d8b78`](https://github.com/itachi1706/SingBuses/commit/c9d8b78b00ed6798b5f26323b96545d43e4f9fee) - Sonar changes *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`bbaf659`](https://github.com/itachi1706/SingBuses/commit/bbaf6592485bd1b722cc7d27f11553cc1d70cc4f) - Set exported to false *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`ade0118`](https://github.com/itachi1706/SingBuses/commit/ade011889cebd993369c79b836118fb00e33b237) - Removed unused colors *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`76f3063`](https://github.com/itachi1706/SingBuses/commit/76f30637c2bdaf7a0fbadff0854c833f7d2a92e5) - Removed unused strings *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`abf9a87`](https://github.com/itachi1706/SingBuses/commit/abf9a87514a29e5eba3f00a9a8e4168d95aa5f7a) - XML changes *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`e6dcccd`](https://github.com/itachi1706/SingBuses/commit/e6dcccd1b06fd7947f9d14e0227480336a6885a7) - Handle null checks and naming conventions *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`fcc5e02`](https://github.com/itachi1706/SingBuses/commit/fcc5e02bf2fd6013da0de4cf3bf78917e9e19730) - Remove unused imports *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`f9fd591`](https://github.com/itachi1706/SingBuses/commit/f9fd59188c23acee123973341c44c43633336be4) - Formatting *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`9bc1e6e`](https://github.com/itachi1706/SingBuses/commit/9bc1e6e6bdc610e8082521f95411b9761d251db2) - Update obsolete checks *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`f46ab15`](https://github.com/itachi1706/SingBuses/commit/f46ab1551d3210714993caf1778602777e1fe28d) - Reorder dependencies and remove ktx from firebase *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`6ef84b3`](https://github.com/itachi1706/SingBuses/commit/6ef84b3176494d77744c3e6ebaef9041bb586961) - Removed legacy code *(commit by [@itachi1706](https://github.com/itachi1706))*

### :white_check_mark: Tests
- [`0ce56b8`](https://github.com/itachi1706/SingBuses/commit/0ce56b83f733088a44728f328cf2422a1f4780d5) - Update test files *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`4a06bed`](https://github.com/itachi1706/SingBuses/commit/4a06beda3f8144e46c7e6fc6d9ba3d54c66fe89f) - Added some test cases *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`a053710`](https://github.com/itachi1706/SingBuses/commit/a053710f858a480c4f9c25033d21346b69ef79cc) - Added more tests *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`6dcb4d0`](https://github.com/itachi1706/SingBuses/commit/6dcb4d01a60690472dc9ad58b901459262060bac) - Add test cases for new code *(commit by [@itachi1706](https://github.com/itachi1706))*

### :wrench: Chores
- [`4d7fe5d`](https://github.com/itachi1706/SingBuses/commit/4d7fe5d6f5fde4f18d89f62ed3b528b18ea10d70) - **deps**: bump actions/setup-java from 3 to 4 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`4af0745`](https://github.com/itachi1706/SingBuses/commit/4af0745324f67510f25c038cec13a3a299a588dd) - **deps**: bump reactivecircus/android-emulator-runner *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`2557272`](https://github.com/itachi1706/SingBuses/commit/25572726b3ee0f3a613d89d6bd1410bcbcbc6fee) - **deps**: bump github/codeql-action from 2 to 3 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`1962899`](https://github.com/itachi1706/SingBuses/commit/1962899fb3d3aa025eb4b323bfd01e43808830bb) - **deps**: bump actions/upload-artifact from 3 to 4 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`6e97751`](https://github.com/itachi1706/SingBuses/commit/6e97751c527cbc6ed75e15895e0eedb8da760956) - **deps**: bump actions/cache from 3 to 4 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`ce1503b`](https://github.com/itachi1706/SingBuses/commit/ce1503b08445181b6441a19649f570fb0c7f06ac) - **deps**: bump reactivecircus/android-emulator-runner *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`0c2d906`](https://github.com/itachi1706/SingBuses/commit/0c2d90627574e58d39dfca61098804665bb8994f) - **deps**: bump softprops/action-gh-release from 1 to 2 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`62bedfb`](https://github.com/itachi1706/SingBuses/commit/62bedfbfe16740bd4d4565693e8ccfc2bffece63) - **deps**: bump actions/checkout from 4.1.1 to 4.1.6 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`847f73b`](https://github.com/itachi1706/SingBuses/commit/847f73b592d423a4d9b451bbf52ee3ee00cdb04f) - **deps**: bump reactivecircus/android-emulator-runner *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`bdeafcd`](https://github.com/itachi1706/SingBuses/commit/bdeafcd367d8221fb74deb2d342317f586d6fa57) - **deps**: bump actions/checkout from 4.1.6 to 4.1.7 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`3167276`](https://github.com/itachi1706/SingBuses/commit/31672762f8b63fcad55bc8c1343d2f5ae115b94e) - **deps**: Upgraded to AGP 8.5 *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`0f60e3a`](https://github.com/itachi1706/SingBuses/commit/0f60e3a6ad40bccb2040c1a193bd9598094e0609) - Migrate to plugin block management *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`a83c49f`](https://github.com/itachi1706/SingBuses/commit/a83c49fafa80770379f1a4759c34d44261ffbedf) - Upgrade to compile against SDK 34 *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`80a7a39`](https://github.com/itachi1706/SingBuses/commit/80a7a3944544e9780fb1fd771d81bfeac80d4f53) - Add Google Play build variant back *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`0b706f4`](https://github.com/itachi1706/SingBuses/commit/0b706f4a72ec71604a9e7648076f2d6d416d2659) - Switch to use list for the rest as well *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`395e01a`](https://github.com/itachi1706/SingBuses/commit/395e01ac829e042db83ecb39900ab2456800ffbb) - Optimized imports *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`2227f62`](https://github.com/itachi1706/SingBuses/commit/2227f622d43ff66e4523e723b6654324fcdae228) - SGBUSAND-292 Removed jCenter requirement *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`56a279c`](https://github.com/itachi1706/SingBuses/commit/56a279c5d6151083666cfafac7425cb5d31b0323) - Use JDK 17 instead of 11 *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`4a5191a`](https://github.com/itachi1706/SingBuses/commit/4a5191a8a9137e57904f0f062b3d3f82b669f327) - Removed ProgressDialog from Bus Services View *(commit by [@itachi1706](https://github.com/itachi1706))*


## [5.2.2] - 2023-11-27
### :bug: Bug Fixes
- [`903c58a`](https://github.com/itachi1706/SingBuses/commit/903c58a56463dbc4314abb1315b1c512ed920363) - SGBUSAND-283 Updated Gson rules for ProGuard *(commit by [@itachi1706](https://github.com/itachi1706))*


## [5.2.1] - 2023-11-23
### :wrench: Chores
- [`eb25f81`](https://github.com/itachi1706/SingBuses/commit/eb25f812ed4830b6ca63dbfed3e5edc2c758f0cc) - **deps**: bump actions/checkout from 4.1.0 to 4.1.1 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`60dec77`](https://github.com/itachi1706/SingBuses/commit/60dec775b4915906b1c5374c7fcb2471d07ecac5) - **deps**: Updated appupdater from 2.5.0 to 3.0.1 *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`12994f3`](https://github.com/itachi1706/SingBuses/commit/12994f3297bf1f00477245e1f77b562907e78dc9) - **release**: 5.2.1 bug fix *(commit by [@itachi1706](https://github.com/itachi1706))*


## [5.2.0] - 2023-11-21
### :sparkles: New Features
- [`28a96e3`](https://github.com/itachi1706/SingBuses/commit/28a96e3d89be8fd236ea32af03a054df3b572d49) - SGBUSAND-270 Upgrade to SDK 33 *(commit by [@itachi1706](https://github.com/itachi1706))*

### :bug: Bug Fixes
- [`25ee450`](https://github.com/itachi1706/SingBuses/commit/25ee450e36c4024aab2cae929b5afd0c9d979779) - Fixed Proguard rules *(commit by [@itachi1706](https://github.com/itachi1706))*

### :wrench: Chores
- [`5335dd2`](https://github.com/itachi1706/SingBuses/commit/5335dd26bd48a7448029db1d54b6510991f79e7f) - **deps**: bump reactivecircus/android-emulator-runner *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`4f95e7e`](https://github.com/itachi1706/SingBuses/commit/4f95e7e10f6e7c0156de6014a8a72ea90baf6c14) - **deps**: bump actions/checkout from 3.3.0 to 3.5.3 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`434c592`](https://github.com/itachi1706/SingBuses/commit/434c592ac75b40ccaee66c3a7d3073a204358907) - **deps**: bump actions/checkout from 3.5.3 to 4.1.0 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`cbefb09`](https://github.com/itachi1706/SingBuses/commit/cbefb0989f0d85c5af697f3fb25479ab5124f29b) - Upgrade android build tools to 7.4.2 *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`d83c51c`](https://github.com/itachi1706/SingBuses/commit/d83c51cb52bd367d1d8a52695c3a75d06e1a5c7a) - Upgrade to AGP 8.1.0 *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`75f9be1`](https://github.com/itachi1706/SingBuses/commit/75f9be10c7379435ccb5bbdf02dcb26fa0ada90d) - Upgrade AGP to 8.1.2 *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`feef886`](https://github.com/itachi1706/SingBuses/commit/feef886c8f59b04dd1f5b1ab0626f9a23fc66bcc) - Removed migration code *(commit by [@itachi1706](https://github.com/itachi1706))*
- [`c9bfb5b`](https://github.com/itachi1706/SingBuses/commit/c9bfb5b3d675e55cc9fcfd2599d6d1998694c0f8) - **deps**: bump stefanzweifel/git-auto-commit-action from 4 to 5 *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*
- [`5240aa0`](https://github.com/itachi1706/SingBuses/commit/5240aa0048c580814b0c340fde7d96d4a7a0f3f3) - **release**: 5.2.0 update *(commit by [@itachi1706](https://github.com/itachi1706))*


[5.2.0]: https://github.com/itachi1706/SingBuses/compare/5.1.0...5.2.0
[5.2.1]: https://github.com/itachi1706/SingBuses/compare/5.2.0...5.2.1
[5.2.2]: https://github.com/itachi1706/SingBuses/compare/5.2.1...5.2.2
[5.2.3]: https://github.com/itachi1706/SingBuses/compare/5.2.2...5.2.3
