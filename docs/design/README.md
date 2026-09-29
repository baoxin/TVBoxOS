# TVBox 新 UI 改造

本分支（`feature/newui`）用于基于新设计稿对 TVBox 进行 UI 重构。业务逻辑与数据层保持不动，只调整界面层。

## 目录约定

- `docs/design/` — 存放新 UI 设计稿（图片、切图、标注导出等），按页面命名，如 `home.png`、`detail.png`
- 本 README 下方维护「改造范围清单」，逐页记录状态

## UI 代码地图

| 内容 | 位置 |
|------|------|
| 界面 Activity | `app/src/main/java/com/github/tvbox/osc/ui/activity/` |
| 弹窗 | `app/src/main/java/com/github/tvbox/osc/ui/dialog/` |
| 列表适配器 | `app/src/main/java/com/github/tvbox/osc/ui/adapter/` |
| Fragment | `app/src/main/java/com/github/tvbox/osc/ui/fragment/` |
| TV(Leanback) 界面 | `app/src/main/java/com/github/tvbox/osc/ui/tv/` |
| 布局 XML | `app/src/main/res/layout/`（共 76 个） |
| 播放器控制层 | `app/src/main/java/com/github/tvbox/osc/player/controller/` |
| 主题/颜色/样式 | `app/src/main/res/values/`（styles.xml / colors.xml 等） |

## 核心页面（改造重点）

| 页面 | 布局 | Activity |
|------|------|----------|
| 首页 | `activity_home.xml` | `HomeActivity` |
| 详情 | `activity_detail.xml` | `DetailActivity` |
| 播放 | `activity_play.xml` + `box_vod_control_view.xml` | `PlayActivity` |
| 搜索 | `activity_search.xml` / `activity_fast_search.xml` | `SearchActivity` / `FastSearchActivity` |
| 直播 | `activity_live_play.xml` | `LivePlayActivity` |
| 历史/收藏/设置 | `activity_history.xml` / `activity_collect.xml` / `activity_setting.xml` | 对应 Activity |

## 改造范围清单（随进度更新）

- [ ] 首页
- [ ] 详情页
- [ ] 播放页（含控制条）
- [ ] 搜索 / 快搜
- [ ] 直播
- [ ] 历史 / 收藏 / 设置
- [ ] 全局主题（颜色、字体、圆角、焦点态）

## 工作方式

1. 设计稿放入 `docs/design/`，并在上方清单登记
2. 每个页面独立提交，提交信息注明页面，如 `newui: 首页布局重构`
3. 只动 `res/layout`、`res/values`（主题资源）、`ui/` 下界面代码；不动 `data/`、`server/`、`player/` 核心逻辑
