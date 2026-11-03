package com.ascoltare.music;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
    private static final int BG = 0xFF080909;
    private static final int SURFACE = 0xFF242625;
    private static final int TEXT = 0xFFF7F8F7;
    private static final int MUTED = 0xFFA3A6A4;
    private static final int GREEN = 0xFF1ED760;
    private final Handler handler = new Handler();
    private final View[] pages = new View[3];
    private final LinearLayout[] navItems = new LinearLayout[3];
    private float density;
    private float uiScale;
    private FrameLayout root;
    private View playerScreen;
    private View playlistDetailScreen;
    private ImageView playlistCoverImage;
    private TextView playlistDetailName;
    private TextView playlistDetailCount;
    private LinearLayout playlistDetailTracks;
    private String currentPlaylistId;
    private String currentPlaylistName;
    private static final int REQUEST_PLAYLIST_COVER = 41;
    private GlyphView miniPlayIcon;
    private GlyphView mainPlayIcon;
    private GlyphView miniLikeIcon;
    private GlyphView playerLikeIcon;
    private SeekBar progress;
    private TextView currentTime;
    private TextView totalTime;
    private TextView miniTrackTitle;
    private TextView miniTrackArtist;
    private TextView playerTrackTitle;
    private TextView playerTrackArtist;
    private TextView playerSourceLabel;
    private TextView repeatOneBadge;
    private GlyphView shuffleIcon;
    private GlyphView repeatIcon;
    private ImageView miniCoverImage;
    private ImageView playerCoverImage;
    private EditText searchInput;
    private LinearLayout searchResultsContainer;
    private LinearLayout defaultSearchContent;
    private LinearLayout libraryListContainer;
    private ScrollView playerScrollView;
    private ScrollView lyricsScrollView;
    private LinearLayout lyricsContainer;
    private final List<TextView> lyricViews = new ArrayList<>();
    private List<Lyrics.Entry> lyricsEntries = new ArrayList<>();
    private int currentLyricIndex = -1;
    private boolean lyricsUserScrolling;
    private final Runnable lyricsResume = () -> {
        lyricsUserScrolling = false;
        applyLyricStyles();
        centerCurrentLyric();
    };
    private MusicApi musicApi;
    private LibraryStore libraryStore;
    private MediaPlayer mediaPlayer;
    private Track currentTrack;
    private String currentSource = "搜索";
    private List<Track> searchTracks = new ArrayList<>();
    private List<Track> playbackQueue = new ArrayList<>();
    private int playbackQueueIndex = -1;
    private boolean shuffleEnabled;
    private int repeatMode;
    private boolean playing;
    private boolean mediaPrepared;
    private int playbackRequest;
    private int searchRequest;
    private final Runnable progressTicker = new Runnable() {
        @Override public void run() {
            if (!playing || !mediaPrepared) return;
            int position = mediaPlayer.getCurrentPosition() / 1000;
            progress.setProgress(position);
            updateLyricHighlight(position);
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        density = getResources().getDisplayMetrics().density;
        float screenWidthDp = getResources().getConfiguration().screenWidthDp;
        uiScale = Math.max(0.82f, Math.min(1.35f, screenWidthDp / 360f));
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        setContentView(root);
        musicApi = new MusicApi();
        libraryStore = new LibraryStore(this);
        mediaPlayer = new MediaPlayer();
        setupMediaPlayer();

        pages[0] = createHomePage();
        pages[1] = createSearchPage();
        pages[2] = createLibraryPage();
        for (int i = 0; i < pages.length; i++) {
            root.addView(pages[i], new FrameLayout.LayoutParams(-1, -1));
            pages[i].setVisibility(i == 0 ? View.VISIBLE : View.GONE);
        }
        root.addView(createBottomDock(), bottomParams(132));
        playerScreen = createPlayerScreen();
        root.addView(playerScreen, new FrameLayout.LayoutParams(-1, -1));
        playerScreen.setVisibility(View.GONE);
        playlistDetailScreen = createPlaylistDetailScreen();
        root.addView(playlistDetailScreen, new FrameLayout.LayoutParams(-1, -1));
        playlistDetailScreen.setVisibility(View.GONE);
        refreshLibrary();
    }

    private View createHomePage() {
        LinearLayout content = column(16, 16, 150);
        LinearLayout header = row(Gravity.CENTER_VERTICAL);
        TextView brand = text("Ascoltare", 20, TEXT, true);
        header.addView(brand);
        content.addView(header, new LinearLayout.LayoutParams(-1, dp(55)));

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 6 ? "夜深了" : hour < 11 ? "早上好" : hour < 14 ? "中午好" : hour < 18 ? "下午好" : "晚上好";
        TextView title = text(greeting, 26, TEXT, true);
        content.addView(title);
        margin(title, 0, 5, 0, 17);

        String[][] items = {{"我喜欢的音乐", "深夜电台"}, {"清晨漫游", "Blue Room"}, {"独立遐想", "慵懒节奏"}};
        String[][] arts = {{"lime", "night"}, {"sun", "blue"}, {"mono", "pink"}};
        for (int r = 0; r < 3; r++) {
            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            for (int c = 0; c < 2; c++) {
                LinearLayout item = row(Gravity.CENTER_VERTICAL);
                item.setBackground(round(SURFACE, 5));
                CoverView art = cover(arts[r][c]);
                add(item, art, 54, 54);
                TextView label = text(items[r][c], 12, TEXT, true);
                item.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
                margin(label, 10, 0, 5, 0);
                clickable(item, v -> showToast("UI 预览：音频将在下一步接入"));
                LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0, dp(54), 1);
                itemParams.setMargins(c == 0 ? 0 : dp(4), dp(4), c == 0 ? dp(4) : 0, dp(4));
                line.addView(item, itemParams);
            }
            content.addView(line);
        }

        content.addView(createFeatureCard());
        content.addView(createSection("继续你的音乐旅程", "最近播放",
                new String[]{"After Hours", "Golden Hour", "蓝色房间", "Slow Dance"},
                new String[]{"The Weeknd", "JVKE", "陈婧霏", "AJ Mitchell"},
                new String[]{"night", "sun", "blue", "pink"}));
        content.addView(createSection("懂你所爱", "为你精选",
                new String[]{"每日 mix 01", "独立遐想", "山野来信"},
                new String[]{"独立、流行与电子", "给灵魂放个假", "自然、民谣与远方"},
                new String[]{"mix", "mono", "forest"}));

        LinearLayout releaseHeading = sectionHeading("本周不容错过", "新碟速递");
        margin(releaseHeading, 0, 30, 0, 0);
        content.addView(releaseHeading);
        LinearLayout release = row(Gravity.CENTER_VERTICAL);
        release.setPadding(dp(10), dp(10), dp(10), dp(10));
        release.setBackground(round(0xFF161817, 9));
        add(release, cover("violet"), 70, 70);
        LinearLayout releaseText = column(0, 0, 0);
        releaseText.addView(text("全新专辑", 9, 0xFFC7A7FF, false));
        releaseText.addView(text("Velvet Skies", 15, TEXT, true));
        releaseText.addView(text("Jonas Blue · 2025", 11, MUTED, false));
        release.addView(releaseText, new LinearLayout.LayoutParams(0, -2, 1));
        margin(releaseText, 13, 0, 0, 0);
        add(release, glyph(GlyphView.CHEVRON, MUTED), 20, 20);
        content.addView(release);
        margin(release, 0, 12, 0, 0);
        return scroll(content);
    }

    private View createFeatureCard() {
        LinearLayout card = row(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(20), 0, dp(14), 0);
        card.setBackground(round(0xFF173526, 12));
        LinearLayout copy = column(0, 0, 0);
        copy.addView(text("为 你 定 制", 9, 0xFF92D4A9, false));
        TextView heading = text("今天，听见\n不一样的自己", 22, TEXT, true);
        copy.addView(heading);
        margin(heading, 0, 8, 0, 6);
        copy.addView(text("根据你的音乐口味每日更新", 10, 0xFFA8B9AE, false));
        FrameLayout play = iconButton(GlyphView.PLAY, 0xFF07160C, 34, 16);
        play.setBackground(round(GREEN, 100));
        copy.addView(play);
        margin(play, 0, 13, 0, 0);
        play.setOnClickListener(v -> setPlaying(true));
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        CoverView art = cover("daily");
        art.setRotation(7);
        add(card, art, 140, 140);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(174));
        params.setMargins(0, dp(24), 0, 0);
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout createSection(String kicker, String title, String[] names, String[] subtitles, String[] arts) {
        LinearLayout section = column(0, 0, 0);
        section.addView(sectionHeading(kicker, title));
        LinearLayout cards = row(Gravity.TOP);
        for (int i = 0; i < names.length; i++) {
            LinearLayout card = column(0, 0, 0);
            CoverView art = cover(arts[i]);
            add(card, art, 140, 140);
            TextView name = text(names[i], 13, TEXT, true);
            card.addView(name);
            margin(name, 0, 10, 0, 4);
            TextView subtitle = text(subtitles[i], 11, MUTED, false);
            subtitle.setSingleLine(true);
            card.addView(subtitle);
            card.setOnClickListener(v -> openPlayer());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dp(140), -2);
            cardParams.setMargins(0, 0, dp(15), 0);
            cards.addView(card, cardParams);
        }
        HorizontalScrollView scroller = horizontalScroll(cards);
        section.addView(scroller);
        margin(scroller, 0, 14, 0, 0);
        return section;
    }

    private LinearLayout sectionHeading(String kicker, String title) {
        LinearLayout heading = column(0, 0, 0);
        heading.addView(text(kicker, 10, MUTED, false));
        heading.addView(text(title, 21, TEXT, true));
        margin(heading, 0, 30, 0, 0);
        return heading;
    }

    private View createSearchPage() {
        LinearLayout content = column(18, 28, 150);
        content.addView(text("搜索", 30, TEXT, true));
        LinearLayout search = row(Gravity.CENTER_VERTICAL);
        search.setPadding(dp(12), 0, dp(6), 0);
        search.setBackground(round(0xFFF2F3F2, 5));
        searchInput = new EditText(this);
        searchInput.setTextColor(0xFF222222);
        searchInput.setHintTextColor(0xFF4B4E4B);
        searchInput.setHint("输入歌曲或歌手");
        searchInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13 * uiScale);
        searchInput.setSingleLine(true);
        searchInput.setPadding(dp(10), 0, dp(10), 0);
        searchInput.setBackground(null);
        searchInput.setInputType(InputType.TYPE_CLASS_TEXT);
        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.addView(searchInput, new LinearLayout.LayoutParams(0, -2, 1));
        FrameLayout searchButton = iconButton(GlyphView.SEARCH, 0xFF282A28, 36, 23);
        searchButton.setOnClickListener(v -> performSearch());
        add(search, searchButton, 36, 42);
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
        content.addView(search, new LinearLayout.LayoutParams(-1, dp(44)));
        margin(search, 0, 22, 0, 0);

        defaultSearchContent = column(0, 0, 0);
        TextView recentTitle = text("最近搜索", 18, TEXT, true);
        defaultSearchContent.addView(recentTitle);
        margin(recentTitle, 0, 30, 0, 16);
        defaultSearchContent.addView(libraryRow("night", "After Hours", "歌曲 · The Weeknd"));
        defaultSearchContent.addView(libraryRow("blue", "陈婧霏", "艺人"));
        TextView browseTitle = text("浏览全部", 18, TEXT, true);
        defaultSearchContent.addView(browseTitle);
        margin(browseTitle, 0, 28, 0, 14);

        String[] names = {"播客", "为你打造", "排行榜", "新歌", "心情", "摇滚", "专注", "睡眠"};
        String[] arts = {"mono", "daily", "blue", "pink", "sun", "violet", "forest", "night"};
        int[] colors = {0xFF147844, 0xFF674597, 0xFF326B95, 0xFFA63E71, 0xFFAA6334, 0xFF923F44, 0xFF31766B, 0xFF4C5694};
        for (int i = 0; i < names.length; i += 2) {
            LinearLayout line = row(Gravity.TOP);
            line.addView(browseCard(names[i], arts[i], colors[i]), weightedParams(100, 0, 0, 6, 6));
            line.addView(browseCard(names[i + 1], arts[i + 1], colors[i + 1]), weightedParams(100, 6, 0, 0, 6));
            defaultSearchContent.addView(line);
        }
        content.addView(defaultSearchContent);
        searchResultsContainer = column(0, 0, 0);
        searchResultsContainer.setVisibility(View.GONE);
        content.addView(searchResultsContainer);
        return scroll(content);
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            showToast("请输入歌曲或歌手");
            return;
        }
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        int request = ++searchRequest;
        defaultSearchContent.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.VISIBLE);
        searchResultsContainer.removeAllViews();
        searchResultsContainer.addView(text("正在搜索…", 14, MUTED, false));
        musicApi.search(query, new MusicApi.Callback<List<Track>>() {
            @Override public void onSuccess(List<Track> tracks) {
                if (request != searchRequest) return;
                searchTracks = new ArrayList<>(tracks);
                searchResultsContainer.removeAllViews();
                TextView summary = text(tracks.isEmpty() ? "没有找到相关歌曲" : "找到 " + tracks.size() + " 首歌曲", 14, MUTED, false);
                searchResultsContainer.addView(summary);
                margin(summary, 0, 24, 0, 18);
                for (Track track : tracks) searchResultsContainer.addView(trackRow(track));
            }

            @Override public void onError(String message) {
                if (request != searchRequest) return;
                searchResultsContainer.removeAllViews();
                searchResultsContainer.addView(text("搜索失败：" + message, 14, MUTED, false));
            }
        });
    }

    private LinearLayout trackRow(Track track) {
        LinearLayout row = row(Gravity.CENTER_VERTICAL);
        ImageView artwork = new ImageView(this);
        artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setBackgroundColor(0xFF332536);
        ImageLoader.load(track.coverUrl, artwork);
        add(row, artwork, 56, 56);
        LinearLayout labels = column(0, 0, 0);
        TextView title = text(track.title, 13, TEXT, false);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        labels.addView(title);
        TextView subtitle = text(track.artist + " · " + track.album, 10, MUTED, false);
        subtitle.setSingleLine(true);
        subtitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        labels.addView(subtitle);
        margin(subtitle, 0, 5, 0, 0);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        margin(labels, 12, 0, 8, 0);
        FrameLayout more = iconButton(GlyphView.MORE, MUTED, 40, 22);
        more.setOnClickListener(v -> showTrackOptions(track));
        add(row, more, 40, 48);
        row.setOnClickListener(v -> playTrackFromQueue(searchTracks, indexOfTrack(searchTracks, track), "搜索"));
        margin(row, 0, 0, 0, 16);
        return row;
    }

    private FrameLayout browseCard(String name, String artName, int color) {
        FrameLayout card = new FrameLayout(this);
        card.setBackground(round(color, 8));
        TextView title = text(name, 16, TEXT, true);
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(-2, -2);
        titleParams.setMargins(dp(13), dp(14), 0, 0);
        card.addView(title, titleParams);
        CoverView art = cover(artName);
        art.setRotation(24);
        FrameLayout.LayoutParams artParams = new FrameLayout.LayoutParams(dp(65), dp(65), Gravity.END | Gravity.BOTTOM);
        artParams.setMargins(0, 0, -dp(10), -dp(10));
        card.addView(art, artParams);
        clickable(card, v -> showToast("已选择「" + name + "」分类"));
        return card;
    }

    private View createLibraryPage() {
        LinearLayout content = column(18, 28, 150);
        LinearLayout header = row(Gravity.CENTER_VERTICAL);
        TextView brand = text("Ascoltare", 18, TEXT, true);
        header.addView(brand);
        TextView title = text("我的音乐库", 23, TEXT, true);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        margin(title, 12, 0, 0, 0);
        FrameLayout add = iconButton(GlyphView.PLUS, TEXT, 40, 27);
        header.addView(add);
        add.setOnClickListener(v -> showCreatePlaylistDialog(null));
        content.addView(header);
        libraryListContainer = column(0, 0, 0);
        content.addView(libraryListContainer);
        margin(libraryListContainer, 0, 30, 0, 0);
        return scroll(content);
    }

    private void refreshLibrary() {
        if (libraryListContainer == null) return;
        libraryListContainer.removeAllViews();
        List<Track> liked = libraryStore.getLiked();
        libraryListContainer.addView(collectionRow("lime", "我喜欢的音乐", liked.size() + " 首歌曲",
                v -> openPlaylistDetail("liked")));
        for (Playlist playlist : libraryStore.getPlaylists()) {
            libraryListContainer.addView(collectionRow("mix", playlist.name, playlist.tracks.size() + " 首歌曲",
                    v -> openPlaylistDetail(playlist.id)));
        }
    }

    private LinearLayout collectionRow(String art, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = row(Gravity.CENTER_VERTICAL);
        add(row, cover(art), 56, 56);
        LinearLayout labels = column(0, 0, 0);
        labels.addView(text(title, 13, TEXT, false));
        TextView sub = text(subtitle, 10, MUTED, false);
        labels.addView(sub);
        margin(sub, 0, 5, 0, 0);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        margin(labels, 12, 0, 0, 0);
        add(row, glyph(GlyphView.CHEVRON, MUTED), 20, 20);
        row.setOnClickListener(listener);
        margin(row, 0, 0, 0, 18);
        return row;
    }

    private View createPlaylistDetailScreen() {
        ScrollView screen = new ScrollView(this);
        screen.setBackgroundColor(BG);
        LinearLayout content = column(18, 20, 40);
        LinearLayout topBar = row(Gravity.CENTER_VERTICAL);
        FrameLayout back = iconButton(GlyphView.DOWN, TEXT, 44, 25);
        back.setRotation(90);
        back.setOnClickListener(v -> closePlaylistDetail());
        topBar.addView(back);
        TextView pageTitle = text("歌单", 16, TEXT, true);
        topBar.addView(pageTitle, new LinearLayout.LayoutParams(0, -2, 1));
        margin(pageTitle, 10, 0, 0, 0);
        content.addView(topBar, new LinearLayout.LayoutParams(-1, dp(46)));

        LinearLayout header = row(Gravity.CENTER_VERTICAL);
        FrameLayout coverButton = new FrameLayout(this);
        coverButton.addView(cover("mix"), new FrameLayout.LayoutParams(-1, -1));
        playlistCoverImage = new ImageView(this);
        playlistCoverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coverButton.addView(playlistCoverImage, new FrameLayout.LayoutParams(-1, -1));
        coverButton.setOnClickListener(v -> choosePlaylistCover());
        add(header, coverButton, 120, 120);
        LinearLayout info = column(0, 0, 0);
        LinearLayout nameRow = row(Gravity.CENTER_VERTICAL);
        playlistDetailName = text("", 23, TEXT, true);
        playlistDetailName.setSingleLine(true);
        playlistDetailName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        nameRow.addView(playlistDetailName, new LinearLayout.LayoutParams(0, -2, 1));
        GlyphView edit = glyph(GlyphView.EDIT, MUTED);
        edit.setTag("rename");
        edit.setOnClickListener(v -> showRenamePlaylistDialog());
        add(nameRow, edit, 22, 22);
        info.addView(nameRow);
        playlistDetailCount = text("", 12, MUTED, false);
        info.addView(playlistDetailCount);
        margin(playlistDetailCount, 0, 10, 0, 10);
        info.addView(text("点击封面可更换图片", 10, MUTED, false));
        header.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        margin(info, 18, 0, 0, 0);
        content.addView(header);
        margin(header, 0, 28, 0, 0);

        TextView songsTitle = text("歌曲", 18, TEXT, true);
        content.addView(songsTitle);
        margin(songsTitle, 0, 32, 0, 18);
        playlistDetailTracks = column(0, 0, 0);
        content.addView(playlistDetailTracks);
        screen.addView(content);
        return screen;
    }

    private void openPlaylistDetail(String id) {
        currentPlaylistId = id;
        List<Track> tracks;
        if ("liked".equals(id)) {
            currentPlaylistName = "我喜欢的音乐";
            tracks = libraryStore.getLiked();
        } else {
            Playlist selected = null;
            for (Playlist playlist : libraryStore.getPlaylists()) {
                if (playlist.id.equals(id)) selected = playlist;
            }
            if (selected == null) return;
            currentPlaylistName = selected.name;
            tracks = selected.tracks;
        }
        playlistDetailName.setText(currentPlaylistName);
        playlistDetailCount.setText(tracks.size() + " 首歌曲");
        ImageLoader.load(libraryStore.getPlaylistCover(id), playlistCoverImage);
        View rename = playlistDetailScreen.findViewWithTag("rename");
        if (rename != null) rename.setVisibility("liked".equals(id) ? View.GONE : View.VISIBLE);
        playlistDetailTracks.removeAllViews();
        if (tracks.isEmpty()) playlistDetailTracks.addView(text("暂无歌曲，可在搜索结果中添加。", 13, MUTED, false));
        for (int i = 0; i < tracks.size(); i++) {
            int index = i;
            LinearLayout row = trackRow(tracks.get(i));
            row.setOnClickListener(v -> {
                closePlaylistDetail();
                playTrackFromQueue(tracks, index, currentPlaylistName);
            });
            playlistDetailTracks.addView(row);
        }
        playlistDetailScreen.setVisibility(View.VISIBLE);
        playlistDetailScreen.setTranslationX(root.getWidth());
        playlistDetailScreen.animate().translationX(0).setDuration(260).start();
    }

    private void closePlaylistDetail() {
        playlistDetailScreen.animate().translationX(root.getWidth()).setDuration(220)
                .withEndAction(() -> playlistDetailScreen.setVisibility(View.GONE)).start();
    }

    private void choosePlaylistCover() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PLAYLIST_COVER);
    }

    private void showRenamePlaylistDialog() {
        LinearLayout body = column(0, 0, 0);
        EditText input = new EditText(this);
        input.setText(currentPlaylistName);
        input.setTextColor(TEXT);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * uiScale);
        input.setSingleLine(true);
        input.setPadding(dp(16), 0, dp(16), 0);
        input.setBackground(round(0xFF3D3D3D, 7));
        body.addView(input, new LinearLayout.LayoutParams(-1, dp(52)));
        Dialog dialog = showStyledDialog("编辑歌单名", body);
        body.addView(dialogButtons(dialog, "保存", v -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                showToast("请输入歌单名称");
                return;
            }
            libraryStore.renamePlaylist(currentPlaylistId, name);
            currentPlaylistName = name;
            playlistDetailName.setText(name);
            refreshLibrary();
            dialog.dismiss();
            showToast("歌单名已更新");
        }));
    }

    private Dialog showStyledDialog(String title, View body) {
        Dialog dialog = new Dialog(this);
        LinearLayout card = column(20, 20, 12);
        card.setBackground(round(0xFF282828, 14));
        TextView heading = text(title, 22, TEXT, true);
        card.addView(heading);
        margin(heading, 0, 0, 0, 18);
        card.addView(body, new LinearLayout.LayoutParams(-1, -2));
        FrameLayout container = new FrameLayout(this);
        container.setPadding(dp(18), dp(24), dp(18), dp(24));
        container.addView(card, new FrameLayout.LayoutParams(-1, -2));
        dialog.setContentView(container);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.7f);
            window.setGravity(Gravity.CENTER);
        }
        dialog.show();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    private TextView dialogButton(String label, int color, View.OnClickListener listener) {
        TextView button = text(label, 14, color, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(14), dp(12), dp(14), dp(12));
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout dialogButtons(Dialog dialog, String positive, View.OnClickListener listener) {
        LinearLayout buttons = row(Gravity.END | Gravity.CENTER_VERTICAL);
        buttons.addView(dialogButton("取消", MUTED, v -> dialog.dismiss()));
        buttons.addView(dialogButton(positive, GREEN, listener));
        margin(buttons, 0, 16, 0, 0);
        return buttons;
    }

    private void showMessageDialog(String title, String message) {
        LinearLayout body = column(0, 0, 0);
        TextView content = text(message, 14, MUTED, false);
        content.setLineSpacing(0, 1.35f);
        body.addView(content);
        Dialog dialog = showStyledDialog(title, body);
        LinearLayout buttons = row(Gravity.END | Gravity.CENTER_VERTICAL);
        buttons.addView(dialogButton("知道了", GREEN, v -> dialog.dismiss()));
        body.addView(buttons);
        margin(buttons, 0, 16, 0, 0);
    }

    private void showCreatePlaylistDialog(Track pendingTrack) {
        LinearLayout body = column(0, 0, 0);
        EditText input = new EditText(this);
        input.setHint("歌单名称");
        input.setHintTextColor(MUTED);
        input.setTextColor(TEXT);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * uiScale);
        input.setSingleLine(true);
        input.setPadding(dp(16), 0, dp(16), 0);
        input.setBackground(round(0xFF3D3D3D, 7));
        body.addView(input, new LinearLayout.LayoutParams(-1, dp(52)));
        Dialog dialog = showStyledDialog("创建歌单", body);
        body.addView(dialogButtons(dialog, "创建", v -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                showToast("请输入歌单名称");
                return;
            }
            Playlist playlist = libraryStore.createPlaylist(name);
            if (pendingTrack != null) libraryStore.addToPlaylist(playlist.id, pendingTrack);
            refreshLibrary();
            dialog.dismiss();
            showToast(pendingTrack == null ? "歌单已创建" : "已创建歌单并添加歌曲");
        }));
        input.requestFocus();
    }

    private void showAddToPlaylist(Track track) {
        List<Playlist> playlists = libraryStore.getPlaylists();
        if (playlists.isEmpty()) {
            LinearLayout body = column(0, 0, 0);
            body.addView(text("还没有歌单，是否先创建一个？", 14, MUTED, false));
            Dialog dialog = showStyledDialog("添加到歌单", body);
            body.addView(dialogButtons(dialog, "创建歌单", v -> {
                dialog.dismiss();
                showCreatePlaylistDialog(track);
            }));
            return;
        }
        String[] names = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) names[i] = playlists.get(i).name;
        showListDialog("添加到歌单", names, index -> {
            Playlist playlist = playlists.get(index);
            libraryStore.addToPlaylist(playlist.id, track);
            refreshLibrary();
            showToast("已添加到「" + playlist.name + "」");
        });
    }

    private void showTrackCollection(String name, List<Track> tracks) {
        if (tracks.isEmpty()) {
            showMessageDialog(name, "暂无歌曲，可在搜索结果中添加。");
            return;
        }
        String[] names = new String[tracks.size()];
        for (int i = 0; i < tracks.size(); i++) names[i] = tracks.get(i).title + " — " + tracks.get(i).artist;
        showListDialog(name, names, index -> playTrackFromQueue(tracks, index, name));
    }

    private interface DialogItemListener { void onSelect(int index); }

    private void showListDialog(String title, String[] items, DialogItemListener listener) {
        LinearLayout list = column(0, 0, 0);
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(list, new ScrollView.LayoutParams(-1, -2));
        Dialog dialog = showStyledDialog(title, scroll);
        for (int i = 0; i < items.length; i++) {
            int index = i;
            TextView item = text(items[i], 16, TEXT, false);
            item.setSingleLine(true);
            item.setEllipsize(android.text.TextUtils.TruncateAt.END);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onSelect(index);
            });
            list.addView(item, new LinearLayout.LayoutParams(-1, dp(54)));
        }
        scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, Math.min(dp(320), dp(items.length * 54))));
    }

    private void showTrackOptions(Track track) {
        if (track == null) {
            showToast("请先选择歌曲");
            return;
        }
        Dialog dialog = new Dialog(this);
        LinearLayout sheet = column(12, 16, 16);
        sheet.setBackground(round(0xFF282828, 18));

        LinearLayout trackHeader = row(Gravity.CENTER_VERTICAL);
        ImageView artwork = new ImageView(this);
        artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setBackgroundColor(0xFF332536);
        ImageLoader.load(track.coverUrl, artwork);
        add(trackHeader, artwork, 56, 56);
        LinearLayout labels = column(0, 0, 0);
        TextView title = text(track.title, 17, TEXT, true);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        labels.addView(title);
        TextView artist = text(track.artist, 12, MUTED, false);
        artist.setSingleLine(true);
        labels.addView(artist);
        margin(artist, 0, 6, 0, 0);
        trackHeader.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        margin(labels, 14, 0, 0, 0);
        sheet.addView(trackHeader);
        margin(trackHeader, 0, 0, 0, 12);
        View divider = new View(this);
        divider.setBackgroundColor(0x18FFFFFF);
        sheet.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

        boolean liked = libraryStore.isLiked(track);
        sheet.addView(trackOptionRow(GlyphView.PLUS, "添加到歌单", TEXT, v -> {
            dialog.dismiss();
            showAddToPlaylist(track);
        }));
        sheet.addView(trackOptionRow(GlyphView.HEART, liked ? "从我喜欢中移除" : "添加到我喜欢", liked ? GREEN : TEXT, v -> {
            dialog.dismiss();
            boolean added = libraryStore.toggleLike(track);
            updateLikeIcons();
            refreshLibrary();
            showToast(added ? "已添加到「我喜欢的音乐」" : "已从「我喜欢的音乐」移除");
        }));

        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.65f);
            window.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout trackOptionRow(int icon, String label, int color, View.OnClickListener listener) {
        LinearLayout row = row(Gravity.CENTER_VERTICAL);
        add(row, glyph(icon, color), 25, 25);
        TextView text = text(label, 16, color, false);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        margin(text, 18, 0, 0, 0);
        clickable(row, listener);
        row.setMinimumHeight(dp(58));
        return row;
    }

    private LinearLayout libraryRow(String art, String title, String subtitle) {
        LinearLayout row = row(Gravity.CENTER_VERTICAL);
        add(row, cover(art), 56, 56);
        LinearLayout labels = column(0, 0, 0);
        labels.addView(text(title, 13, TEXT, false));
        TextView sub = text(subtitle, 10, MUTED, false);
        labels.addView(sub);
        margin(sub, 0, 5, 0, 0);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        margin(labels, 12, 0, 0, 0);
        add(row, glyph(GlyphView.MORE, MUTED), 20, 20);
        row.setOnClickListener(v -> openPlayer());
        return row;
    }

    private View createBottomDock() {
        LinearLayout dock = column(0, 0, 0);
        dock.setBackgroundColor(BG);
        LinearLayout mini = row(Gravity.CENTER_VERTICAL);
        mini.setBackground(round(0xFF332536, 7));
        mini.setPadding(0, 0, dp(10), 0);
        FrameLayout miniArt = new FrameLayout(this);
        miniArt.addView(cover("night"), new FrameLayout.LayoutParams(-1, -1));
        miniCoverImage = new ImageView(this);
        miniCoverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        miniArt.addView(miniCoverImage, new FrameLayout.LayoutParams(-1, -1));
        add(mini, miniArt, 58, 58);
        LinearLayout labels = column(0, 0, 0);
        miniTrackTitle = text("After Hours", 12, TEXT, true);
        miniTrackArtist = text("The Weeknd", 10, 0xFFC4B9C5, false);
        labels.addView(miniTrackTitle);
        labels.addView(miniTrackArtist);
        mini.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        margin(labels, 11, 0, 0, 0);
        FrameLayout heart = iconButton(GlyphView.HEART, TEXT, 52, 24);
        miniLikeIcon = (GlyphView) heart.getChildAt(0);
        heart.setOnClickListener(v -> toggleLike());
        add(mini, heart, 52, 58);
        FrameLayout play = iconButton(GlyphView.PLAY, TEXT, 54, 26);
        miniPlayIcon = (GlyphView) ((ViewGroup) play).getChildAt(0);
        play.setOnClickListener(v -> togglePlayback());
        add(mini, play, 54, 58);
        dock.addView(mini, new LinearLayout.LayoutParams(-1, dp(58)));
        margin(mini, 9, 6, 9, 0);
        mini.setOnClickListener(v -> openPlayer());

        LinearLayout nav = row(Gravity.CENTER);
        String[] labelsText = {"首页", "搜索", "音乐库"};
        int[] icons = {GlyphView.HOME, GlyphView.SEARCH, GlyphView.LIBRARY};
        for (int i = 0; i < 3; i++) {
            int index = i;
            LinearLayout item = column(0, 0, 0);
            item.setGravity(Gravity.CENTER);
            GlyphView glyph = glyph(icons[i], i == 0 ? TEXT : MUTED);
            add(item, glyph, 23, 23);
            TextView label = text(labelsText[i], 10, i == 0 ? TEXT : MUTED, false);
            label.setGravity(Gravity.CENTER_HORIZONTAL);
            item.addView(label, new LinearLayout.LayoutParams(-1, -2));
            margin(label, 0, 5, 0, 0);
            item.setOnClickListener(v -> selectPage(index));
            nav.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
            navItems[i] = item;
        }
        dock.addView(nav, new LinearLayout.LayoutParams(-1, dp(68)));
        return dock;
    }

    private View createPlayerScreen() {
        ScrollView screen = new ScrollView(this);
        playerScrollView = screen;
        screen.setFillViewport(true);
        screen.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF493347, 0xFF211925, 0xFF111112}));
        LinearLayout content = column(24, 18, 28);
        LinearLayout header = row(Gravity.CENTER_VERTICAL);
        FrameLayout close = iconButton(GlyphView.DOWN, TEXT, 42, 26);
        close.setOnClickListener(v -> closePlayer());
        header.addView(close);
        LinearLayout heading = column(0, 0, 0);
        heading.setGravity(Gravity.CENTER);
        heading.addView(text("正 在 播 放", 9, 0xFFD1C7D2, false));
        playerSourceLabel = text("搜索", 11, TEXT, true);
        heading.addView(playerSourceLabel);
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        FrameLayout more = iconButton(GlyphView.MORE, TEXT, 42, 24);
        more.setOnClickListener(v -> showTrackOptions(currentTrack));
        header.addView(more);
        content.addView(header, new LinearLayout.LayoutParams(-1, dp(50)));

        SquareFrameLayout artwork = new SquareFrameLayout(this);
        artwork.addView(new CoverView(this, "night"), new FrameLayout.LayoutParams(-1, -1));
        playerCoverImage = new ImageView(this);
        playerCoverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.addView(playerCoverImage, new FrameLayout.LayoutParams(-1, -1));
        content.addView(artwork, new LinearLayout.LayoutParams(-1, -2));
        margin(artwork, 0, 34, 0, 0);

        LinearLayout track = row(Gravity.CENTER_VERTICAL);
        LinearLayout trackText = column(0, 0, 0);
        playerTrackTitle = text("After Hours", 22, TEXT, true);
        playerTrackArtist = text("The Weeknd", 13, 0xFFC5B9C7, false);
        trackText.addView(playerTrackTitle);
        trackText.addView(playerTrackArtist);
        track.addView(trackText, new LinearLayout.LayoutParams(0, -2, 1));
        FrameLayout like = iconButton(GlyphView.HEART, TEXT, 44, 27);
        playerLikeIcon = (GlyphView) like.getChildAt(0);
        like.setOnClickListener(v -> toggleLike());
        track.addView(like);
        content.addView(track);
        margin(track, 0, 28, 0, 18);

        progress = new SeekBar(this);
        progress.setMax(0);
        progress.setProgress(0);
        progress.setProgressTintList(ColorStateList.valueOf(TEXT));
        progress.setProgressBackgroundTintList(ColorStateList.valueOf(0x50FFFFFF));
        progress.setThumbTintList(ColorStateList.valueOf(TEXT));
        content.addView(progress, new LinearLayout.LayoutParams(-1, dp(24)));
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                updateTime(value);
                updateLyricHighlight(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPrepared) mediaPlayer.seekTo(seekBar.getProgress() * 1000);
            }
        });
        LinearLayout times = row(Gravity.CENTER_VERTICAL);
        currentTime = text("0:00", 9, 0xFFB7ACB9, false);
        totalTime = text("0:00", 9, 0xFFB7ACB9, false);
        times.addView(currentTime, new LinearLayout.LayoutParams(0, -2, 1));
        times.addView(totalTime);
        content.addView(times);

        LinearLayout controls = row(Gravity.CENTER_VERTICAL);
        FrameLayout shuffleButton = iconButton(GlyphView.SHUFFLE, 0xFFD2C9D3, 42, 22);
        shuffleIcon = (GlyphView) shuffleButton.getChildAt(0);
        shuffleButton.setOnClickListener(v -> toggleShuffle());
        add(controls, shuffleButton, 0, -1);
        LinearLayout.LayoutParams weight = new LinearLayout.LayoutParams(0, -2, 1);
        View spacer = new View(this); controls.addView(spacer, weight);
        FrameLayout previousButton = iconButton(GlyphView.BACK, TEXT, 46, 31);
        previousButton.setOnClickListener(v -> playPrevious());
        add(controls, previousButton, 0, -1);
        View spacer2 = new View(this); controls.addView(spacer2, new LinearLayout.LayoutParams(0, -2, 1));
        FrameLayout mainPlay = iconButton(GlyphView.PLAY, 0xFF171219, 64, 29);
        mainPlayIcon = (GlyphView) mainPlay.getChildAt(0);
        mainPlay.setBackground(round(Color.WHITE, 100));
        mainPlay.setOnClickListener(v -> togglePlayback());
        add(controls, mainPlay, 64, 64);
        View spacer3 = new View(this); controls.addView(spacer3, new LinearLayout.LayoutParams(0, -2, 1));
        FrameLayout nextButton = iconButton(GlyphView.FORWARD, TEXT, 46, 31);
        nextButton.setOnClickListener(v -> playNext(false));
        add(controls, nextButton, 0, -1);
        View spacer4 = new View(this); controls.addView(spacer4, new LinearLayout.LayoutParams(0, -2, 1));
        FrameLayout repeatButton = iconButton(GlyphView.REPEAT, 0xFFD2C9D3, 42, 22);
        repeatIcon = (GlyphView) repeatButton.getChildAt(0);
        repeatOneBadge = text("1", 8, GREEN, true);
        repeatOneBadge.setVisibility(View.GONE);
        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(dp(12), dp(12), Gravity.END | Gravity.BOTTOM);
        repeatButton.addView(repeatOneBadge, badgeParams);
        repeatButton.setOnClickListener(v -> cycleRepeatMode());
        add(controls, repeatButton, 0, -1);
        content.addView(controls);
        margin(controls, 0, 16, 0, 0);

        LinearLayout footer = row(Gravity.CENTER_VERTICAL);
        View space = new View(this);
        footer.addView(space, new LinearLayout.LayoutParams(0, 1, 1));
        FrameLayout addToPlaylist = iconButton(GlyphView.PLUS, 0xFFC8BDC9, 42, 20);
        addToPlaylist.setOnClickListener(v -> {
            if (currentTrack == null) showToast("请先选择歌曲");
            else showAddToPlaylist(currentTrack);
        });
        footer.addView(addToPlaylist);
        content.addView(footer);
        margin(footer, 0, 18, 0, 0);

        TextView lyricsHeading = text("歌词", 18, TEXT, true);
        lyricsHeading.setOnClickListener(v -> playerScrollView.smoothScrollTo(0, lyricsScrollView.getTop() - dp(50)));
        content.addView(lyricsHeading);
        margin(lyricsHeading, 0, 30, 0, 15);
        lyricsScrollView = new ScrollView(this);
        lyricsScrollView.setVerticalScrollBarEnabled(false);
        lyricsScrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (!lyricsUserScrolling) showAllLyricsClearly();
                lyricsUserScrolling = true;
                handler.removeCallbacks(lyricsResume);
                v.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
                handler.postDelayed(lyricsResume, 4000);
            }
            return false;
        });
        lyricsContainer = column(0, 150, 150);
        TextView initialLyrics = text("选择歌曲后显示歌词", 14, MUTED, false);
        initialLyrics.setGravity(Gravity.CENTER);
        lyricsContainer.addView(initialLyrics);
        lyricsScrollView.addView(lyricsContainer, new ScrollView.LayoutParams(-1, -2));
        content.addView(lyricsScrollView, new LinearLayout.LayoutParams(-1, dp(360)));
        screen.addView(content);
        return screen;
    }

    private void selectPage(int selected) {
        for (int i = 0; i < pages.length; i++) {
            pages[i].setVisibility(i == selected ? View.VISIBLE : View.GONE);
            int color = i == selected ? TEXT : MUTED;
            LinearLayout item = navItems[i];
            ((GlyphView) item.getChildAt(0)).setColor(color);
            ((TextView) item.getChildAt(1)).setTextColor(color);
        }
    }

    private void openPlayer() {
        playerScreen.setVisibility(View.VISIBLE);
        playerScreen.setTranslationY(root.getHeight());
        playerScreen.animate().translationY(0).setDuration(280).start();
    }

    private void closePlayer() {
        playerScreen.animate().translationY(root.getHeight()).setDuration(240).withEndAction(() -> playerScreen.setVisibility(View.GONE)).start();
    }

    private void setupMediaPlayer() {
        mediaPlayer.setOnPreparedListener(player -> {
            mediaPrepared = true;
            int duration = player.getDuration() / 1000;
            progress.setMax(duration);
            progress.setProgress(0);
            totalTime.setText(formatTime(duration));
            player.start();
            setPlaying(true);
        });
        mediaPlayer.setOnCompletionListener(player -> {
            if (repeatMode == 2 && mediaPrepared) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                setPlaying(true);
            } else {
                playNext(true);
            }
        });
        mediaPlayer.setOnErrorListener((player, what, extra) -> {
            mediaPrepared = false;
            setPlaying(false);
            showToast("音频加载失败，请稍后重试");
            return true;
        });
    }

    private void playTrack(Track track) {
        String source = currentTrack != null && currentTrack.getKey().equals(track.getKey()) ? currentSource : "搜索";
        playTrack(track, source);
    }

    private void playTrackFromQueue(List<Track> tracks, int index, String source) {
        if (tracks == null || tracks.isEmpty() || index < 0 || index >= tracks.size()) return;
        playbackQueue = new ArrayList<>(tracks);
        playbackQueueIndex = index;
        playTrack(playbackQueue.get(index), source);
    }

    private int indexOfTrack(List<Track> tracks, Track target) {
        for (int i = 0; i < tracks.size(); i++) if (tracks.get(i).getKey().equals(target.getKey())) return i;
        return -1;
    }

    private void playTrack(Track track, String source) {
        int request = ++playbackRequest;
        currentSource = source;
        playerSourceLabel.setText(source);
        int queueIndex = indexOfTrack(playbackQueue, track);
        if (queueIndex >= 0) playbackQueueIndex = queueIndex;
        else {
            playbackQueue = new ArrayList<>();
            playbackQueue.add(track);
            playbackQueueIndex = 0;
        }
        currentTrack = track;
        mediaPrepared = false;
        mediaPlayer.reset();
        setPlaying(false);
        progress.setMax(0);
        progress.setProgress(0);
        totalTime.setText("0:00");
        updateTrackUi();
        openPlayer();
        showToast("正在获取播放地址…");
        musicApi.getDetails(track, new MusicApi.Callback<Track>() {
            @Override public void onSuccess(Track detailed) {
                if (request != playbackRequest) return;
                currentTrack = detailed;
                updateTrackUi();
                startPlayback(detailed, request);
            }

            @Override public void onError(String message) {
                if (request == playbackRequest) showToast("播放失败：" + message);
            }
        });
    }

    private void playNext(boolean automatic) {
        if (playbackQueue.isEmpty()) {
            if (!automatic) showToast("暂无播放列表");
            return;
        }
        int next = playbackQueueIndex;
        if (shuffleEnabled && playbackQueue.size() > 1) {
            java.util.Random random = new java.util.Random();
            do { next = random.nextInt(playbackQueue.size()); } while (next == playbackQueueIndex);
        } else {
            next++;
            if (next >= playbackQueue.size()) {
                if (repeatMode == 1) next = 0;
                else {
                    if (!automatic) showToast("已经是最后一首");
                    else {
                        setPlaying(false);
                        progress.setProgress(0);
                    }
                    return;
                }
            }
        }
        playTrackFromQueue(playbackQueue, next, currentSource);
    }

    private void playPrevious() {
        if (playbackQueue.isEmpty()) {
            showToast("暂无播放列表");
            return;
        }
        if (mediaPrepared && mediaPlayer.getCurrentPosition() > 5000) {
            mediaPlayer.seekTo(0);
            return;
        }
        int previous = playbackQueueIndex - 1;
        if (previous < 0) {
            if (repeatMode == 1) previous = playbackQueue.size() - 1;
            else {
                showToast("已经是第一首");
                return;
            }
        }
        playTrackFromQueue(playbackQueue, previous, currentSource);
    }

    private void toggleShuffle() {
        shuffleEnabled = !shuffleEnabled;
        shuffleIcon.setColor(shuffleEnabled ? GREEN : 0xFFD2C9D3);
        showToast(shuffleEnabled ? "随机播放已开启" : "随机播放已关闭");
    }

    private void cycleRepeatMode() {
        repeatMode = (repeatMode + 1) % 3;
        repeatIcon.setColor(repeatMode == 0 ? 0xFFD2C9D3 : GREEN);
        repeatOneBadge.setVisibility(repeatMode == 2 ? View.VISIBLE : View.GONE);
        showToast(repeatMode == 0 ? "循环已关闭" : repeatMode == 1 ? "歌单循环" : "单曲循环");
    }

    private void startPlayback(Track track, int request) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(track.audioUrl);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            if (request == playbackRequest) showToast("无法播放该歌曲：" + e.getMessage());
        }
    }

    private void updateTrackUi() {
        if (currentTrack == null) return;
        miniTrackTitle.setText(currentTrack.title);
        miniTrackArtist.setText(currentTrack.artist);
        playerTrackTitle.setText(currentTrack.title);
        playerTrackArtist.setText(currentTrack.artist);
        ImageLoader.load(currentTrack.coverUrl, miniCoverImage);
        ImageLoader.load(currentTrack.coverUrl, playerCoverImage);
        updateLikeIcons();
        updateLyrics();
    }

    private void updateLyrics() {
        lyricsEntries = Lyrics.parse(currentTrack.lyrics);
        lyricsContainer.removeAllViews();
        lyricViews.clear();
        currentLyricIndex = -1;
        lyricsUserScrolling = false;
        handler.removeCallbacks(lyricsResume);
        lyricsScrollView.scrollTo(0, 0);
        if (lyricsEntries.isEmpty()) {
            TextView empty = text("暂无歌词", 14, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            lyricsContainer.addView(empty);
            return;
        }
        for (Lyrics.Entry entry : lyricsEntries) {
            TextView line = text(entry.text, 14, MUTED, false);
            line.setGravity(Gravity.CENTER);
            line.setPadding(0, dp(8), 0, dp(8));
            line.setLineSpacing(0, 1.2f);
            line.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            lyricsContainer.addView(line, new LinearLayout.LayoutParams(-1, -2));
            lyricViews.add(line);
        }
    }

    private void updateLyricHighlight(float seconds) {
        int index = Lyrics.indexAt(lyricsEntries, seconds);
        if (index == currentLyricIndex) return;
        currentLyricIndex = index;
        if (lyricsUserScrolling) showAllLyricsClearly();
        else applyLyricStyles();
        centerCurrentLyric();
    }

    private void showAllLyricsClearly() {
        for (int i = 0; i < lyricViews.size(); i++) {
            TextView line = lyricViews.get(i);
            line.getPaint().setMaskFilter(null);
            line.setAlpha(i == currentLyricIndex ? 1f : 0.88f);
            line.setTextColor(i == currentLyricIndex ? GREEN : 0xFFD8D8D8);
            line.setTextSize(TypedValue.COMPLEX_UNIT_SP, (i == currentLyricIndex ? 16 : 14) * uiScale);
            line.setTypeface(Typeface.create("sans", i == currentLyricIndex ? Typeface.BOLD : Typeface.NORMAL));
        }
    }

    private void applyLyricStyles() {
        for (int i = 0; i < lyricViews.size(); i++) {
            TextView line = lyricViews.get(i);
            line.getPaint().setMaskFilter(null);
            if (i == currentLyricIndex) {
                line.setTextColor(GREEN);
                line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * uiScale);
                line.setTypeface(Typeface.create("sans", Typeface.BOLD));
                line.setAlpha(1f);
                continue;
            }
            line.setTextColor(MUTED);
            line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * uiScale);
            line.setTypeface(Typeface.create("sans", Typeface.NORMAL));
            if (currentLyricIndex >= 0 && i < currentLyricIndex) {
                int distance = currentLyricIndex - i;
                line.setAlpha(Math.max(0.12f, 0.68f - distance * 0.09f));
                float blur = Math.min(4f, 0.3f + distance * 0.35f);
                line.getPaint().setMaskFilter(new BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL));
            } else {
                line.setAlpha(0.68f);
            }
        }
    }

    private void centerCurrentLyric() {
        if (lyricsUserScrolling || currentLyricIndex < 0 || currentLyricIndex >= lyricViews.size()) return;
        TextView current = lyricViews.get(currentLyricIndex);
        int target = current.getTop() + current.getHeight() / 2 - lyricsScrollView.getHeight() / 2;
        lyricsScrollView.smoothScrollTo(0, Math.max(0, target));
    }

    private void togglePlayback() {
        if (currentTrack == null) {
            showToast("请先搜索并选择歌曲");
            return;
        }
        if (mediaPrepared) setPlaying(!playing);
        else playTrack(currentTrack);
    }

    private void setPlaying(boolean value) {
        if (value && !mediaPrepared) {
            showToast("请先选择可播放的歌曲");
            return;
        }
        if (mediaPrepared) {
            if (value) mediaPlayer.start();
            else if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        }
        playing = value;
        handler.removeCallbacks(progressTicker);
        miniPlayIcon.setIcon(value ? GlyphView.PAUSE : GlyphView.PLAY);
        mainPlayIcon.setIcon(value ? GlyphView.PAUSE : GlyphView.PLAY);
        if (value) handler.post(progressTicker);
    }

    private void toggleLike() {
        if (currentTrack == null) {
            showToast("请先选择歌曲");
            return;
        }
        boolean added = libraryStore.toggleLike(currentTrack);
        updateLikeIcons();
        refreshLibrary();
        showToast(added ? "已添加到「我喜欢的音乐」" : "已从「我喜欢的音乐」移除");
    }

    private void updateLikeIcons() {
        if (miniLikeIcon == null || playerLikeIcon == null) return;
        int color = libraryStore.isLiked(currentTrack) ? GREEN : TEXT;
        miniLikeIcon.setColor(color);
        playerLikeIcon.setColor(color);
    }

    private String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    private void updateTime(int seconds) {
        currentTime.setText(formatTime(seconds));
    }

    private LinearLayout column(int horizontalPadding, int topPadding, int bottomPadding) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(horizontalPadding), dp(topPadding), dp(horizontalPadding), dp(bottomPadding));
        return layout;
    }

    private LinearLayout row(int gravity) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(gravity);
        return layout;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size * uiScale);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        view.setIncludeFontPadding(false);
        return view;
    }

    private CoverView cover(String type) { return new CoverView(this, type); }
    private GlyphView glyph(int type, int color) { return new GlyphView(this, type, color); }

    private FrameLayout iconButton(int icon, int color, int size, int iconSize) {
        FrameLayout button = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(iconSize), dp(iconSize), Gravity.CENTER);
        button.addView(glyph(icon, color), params);
        button.setClickable(true);
        return button;
    }

    private ScrollView scroll(View content) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        return scroll;
    }

    private HorizontalScrollView horizontalScroll(View content) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        scroll.addView(content);
        return scroll;
    }

    private GradientDrawable round(int color, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private void clickable(View view, View.OnClickListener listener) {
        view.setClickable(true);
        view.setOnClickListener(listener);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            TypedValue value = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, value, true);
            view.setForeground(getDrawable(value.resourceId));
        }
    }

    private void add(LinearLayout parent, View child, int width, int height) {
        parent.addView(child, new LinearLayout.LayoutParams(width == 0 ? -2 : dp(width), height == -1 ? -2 : dp(height)));
    }

    private LinearLayout.LayoutParams weightedParams(int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(height), 1);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private FrameLayout.LayoutParams bottomParams(int height) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, dp(height), Gravity.BOTTOM);
        return params;
    }

    private void margin(View view, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams current = view.getLayoutParams();
        if (current instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) current;
            params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
            view.setLayoutParams(params);
        }
    }

    private int dp(float value) { return Math.round(value * density * uiScale); }
    private void showToast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PLAYLIST_COVER || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        try {
            getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
        }
        String coverUri = data.getData().toString();
        libraryStore.setPlaylistCover(currentPlaylistId, coverUri);
        ImageLoader.load(coverUri, playlistCoverImage);
        showToast("歌单封面已更新");
    }

    @Override
    public void onBackPressed() {
        if (playlistDetailScreen != null && playlistDetailScreen.getVisibility() == View.VISIBLE) closePlaylistDetail();
        else if (playerScreen != null && playerScreen.getVisibility() == View.VISIBLE) closePlayer();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(progressTicker);
        handler.removeCallbacks(lyricsResume);
        playbackRequest++;
        mediaPlayer.release();
        musicApi.shutdown();
        super.onDestroy();
    }
}
