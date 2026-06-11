# Minecraft Music Player Overlay Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a compact Minecraft-style NetEase Cloud Music overlay with multi-view navigation, search, playback metadata, and a client-side queue.

**Architecture:** Keep the existing `MusicPannel` as the single GUI screen and add internal view state. Keep playback network synchronization unchanged through `MusicMessage`; add only a search method to the music manager. Use Forge 1.12 GUI primitives and the existing scrolling-list components.

**Tech Stack:** Java 8, Minecraft Forge 1.12.2, `GuiScreen`, `GuiButton`, `GuiTextField`, `GuiScrollingList`, Gson, Apache HttpClient.

---

## File Structure

- Modify `src/main/java/team/info/ncmfm/interfaces/IMusicManager.java`
  - Add `SearchSongs(String keywords)`.
- Create `src/main/java/team/info/ncmfm/entity/SearchSongs.java`
  - DTO for `/search` song results.
- Modify `src/main/java/team/info/ncmfm/manager/NeteaseCloudMusicManager.java`
  - Implement `SearchSongs`.
  - Add helper methods for search artist and album extraction.
- Modify `src/main/java/team/info/ncmfm/component/GuiSlotPlayList.java`
  - Accept explicit list bounds from the parent.
  - Retain double-click loading behavior.
- Modify `src/main/java/team/info/ncmfm/component/GuiSlotSubList.java`
  - Accept explicit list bounds from the parent.
  - Retain double-click loading behavior.
- Modify `src/main/java/team/info/ncmfm/component/GuiSlotTracks.java`
  - Accept explicit list bounds from the parent.
  - Render title plus optional artist and album.
- Modify `src/main/java/team/info/ncmfm/ui/MusicPannel.java`
  - Replace the three-column layout with a centered overlay.
  - Add view state, search field, navigation buttons, queue actions, and player bar.

## Task 1: Add NetEase Song Search

**Files:**
- Modify: `src/main/java/team/info/ncmfm/interfaces/IMusicManager.java`
- Create: `src/main/java/team/info/ncmfm/entity/SearchSongs.java`
- Modify: `src/main/java/team/info/ncmfm/manager/NeteaseCloudMusicManager.java`

- [ ] **Step 1: Add the interface method**

In `IMusicManager.java`, add this method near the other content loading methods:

```java
ArrayList<TrackContainer> SearchSongs(String keywords);
```

- [ ] **Step 2: Add the search DTO**

Create `SearchSongs.java` with this shape:

```java
package team.info.ncmfm.entity;

import java.util.List;

public class SearchSongs {
    private int code;
    private Result result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public static class Result {
        private List<Song> songs;

        public List<Song> getSongs() {
            return songs;
        }

        public void setSongs(List<Song> songs) {
            this.songs = songs;
        }
    }

    public static class Song {
        private long id;
        private String name;
        private List<Artist> artists;
        private Album album;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Artist> getArtists() {
            return artists;
        }

        public void setArtists(List<Artist> artists) {
            this.artists = artists;
        }

        public Album getAlbum() {
            return album;
        }

        public void setAlbum(Album album) {
            this.album = album;
        }
    }

    public static class Artist {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Album {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
```

- [ ] **Step 3: Implement `SearchSongs` in the manager**

Add a public method to `NeteaseCloudMusicManager`:

```java
@Override
public ArrayList<TrackContainer> SearchSongs(String keywords) {
    ArrayList<TrackContainer> tracks = new ArrayList<TrackContainer>();
    if (getCachedUserId() == null || isBlank(keywords)) {
        return tracks;
    }

    ApiResult<SearchSongs> result = get(SearchSongs.class, "/search", Arrays.<NameValuePair>asList(
            new BasicNameValuePair("keywords", keywords.trim()),
            new BasicNameValuePair("type", "1"),
            new BasicNameValuePair("limit", "50"),
            new BasicNameValuePair("offset", "0"),
            new BasicNameValuePair("timestamp", Long.toString(System.currentTimeMillis()))
    ));
    if (result.body == null || result.body.getCode() != 200 || result.body.getResult() == null || result.body.getResult().getSongs() == null) {
        return tracks;
    }

    for (SearchSongs.Song song : result.body.getResult().getSongs()) {
        if (song != null) {
            tracks.add(new TrackContainer(
                    song.getId(),
                    safeString(song.getName()),
                    getSearchTrackArtist(song),
                    getSearchTrackAlbum(song)
            ));
        }
    }
    return tracks;
}
```

Add helper methods near the existing track helper methods:

```java
private static String getSearchTrackArtist(SearchSongs.Song track) {
    if (track.getArtists() == null || track.getArtists().isEmpty() || track.getArtists().get(0) == null) {
        return "";
    }
    return safeString(track.getArtists().get(0).getName());
}

private static String getSearchTrackAlbum(SearchSongs.Song track) {
    if (track.getAlbum() == null) {
        return "";
    }
    return safeString(track.getAlbum().getName());
}
```

- [ ] **Step 4: Run compile check**

Run: `./gradlew compileJava`

Expected: Java compilation succeeds, or any failure is unrelated to the new interface and DTO changes.

## Task 2: Make Scrolling Lists Layout-Aware

**Files:**
- Modify: `src/main/java/team/info/ncmfm/component/GuiSlotPlayList.java`
- Modify: `src/main/java/team/info/ncmfm/component/GuiSlotSubList.java`
- Modify: `src/main/java/team/info/ncmfm/component/GuiSlotTracks.java`

- [ ] **Step 1: Update list constructors**

For each list component, replace the current constructor with one that accepts explicit bounds:

```java
public GuiSlotTracks(MusicPannel parent, ArrayList<TrackContainer> trackList, int left, int top, int width, int height, int slotHeight)
{
    super(parent.getMinecraftInstance(), width, parent.height, top, top + height, left, slotHeight, parent.width, parent.height);
    this.parent = parent;
    this.collections = trackList;
    this.slotHeight = slotHeight;
}
```

Use the matching collection type for playlists and albums:

```java
public GuiSlotPlayList(MusicPannel parent, ArrayList<PlayListContainer> collections, int left, int top, int width, int height, int slotHeight)
```

```java
public GuiSlotSubList(MusicPannel parent, ArrayList<SubListContainer> collections, int left, int top, int width, int height, int slotHeight)
```

- [ ] **Step 2: Render richer track rows**

In `GuiSlotTracks.drawSlot`, draw a title row and metadata row:

```java
String title = StringUtils.stripControlCodes(tc.getName());
String artist = StringUtils.stripControlCodes(tc.getAuthor() == null ? "" : tc.getAuthor());
String album = StringUtils.stripControlCodes(tc.getAlbum() == null ? "" : tc.getAlbum());
String meta = artist.length() == 0 ? album : (album.length() == 0 ? artist : artist + " - " + album);
FontRenderer font = this.parent.getFontRenderer();

font.drawString(font.trimStringToWidth(title, listWidth - 12), this.left + 4, slotTop + 2, 0xFFFFFF);
if (meta.length() > 0 && slotHeight >= 22) {
    font.drawString(font.trimStringToWidth(meta, listWidth - 12), this.left + 4, slotTop + 12, 0xAAAAAA);
}
```

- [ ] **Step 3: Run compile check**

Run: `./gradlew compileJava`

Expected: Java compilation succeeds after all constructor call sites are updated in the next task, or fails only because `MusicPannel` still uses old constructors.

## Task 3: Rebuild `MusicPannel` As A Multi-View Overlay

**Files:**
- Modify: `src/main/java/team/info/ncmfm/ui/MusicPannel.java`

- [ ] **Step 1: Add imports**

Add GUI and input imports:

```java
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
```

- [ ] **Step 2: Add view and layout state**

Add constants and fields:

```java
private static final int BUTTON_NAV_PLAYLISTS = 10;
private static final int BUTTON_NAV_ALBUMS = 11;
private static final int BUTTON_NAV_SEARCH = 12;
private static final int BUTTON_NAV_QUEUE = 13;
private static final int BUTTON_ADD_QUEUE = 14;
private static final int BUTTON_PREVIOUS = 15;
private static final int BUTTON_PLAY_SELECTED = 16;
private static final int BUTTON_NEXT = 17;
private static final int BUTTON_CLOSE = 18;
private static final int BUTTON_SEARCH = 19;

private static final int VIEW_PLAYLISTS = 0;
private static final int VIEW_ALBUMS = 1;
private static final int VIEW_TRACKS = 2;
private static final int VIEW_SEARCH = 3;
private static final int VIEW_QUEUE = 4;

private int activeView = VIEW_PLAYLISTS;
private int panelLeft;
private int panelTop;
private int panelWidth;
private int panelHeight;
private int contentLeft;
private int contentTop;
private int contentWidth;
private int contentHeight;
private String viewTitle = "My Playlists";
private String statusMessage = "";
private String currentSource = "Ready";
private TrackContainer currentTrack;
private GuiTextField searchField;
private final ArrayList<TrackContainer> queue = new ArrayList<TrackContainer>();
private int queueIndex = -1;
```

- [ ] **Step 3: Build a centered overlay in `initGui`**

After login and data loading, calculate layout, create navigation buttons, create the text field, and create scrolling lists using explicit bounds.

Use these layout rules:

```java
panelWidth = Math.min(width - 28, 420);
panelHeight = Math.min(height - 24, 260);
panelLeft = (width - panelWidth) / 2;
panelTop = (height - panelHeight) / 2;
int sidebarWidth = 92;
int headerHeight = 28;
int playerHeight = 48;
contentLeft = panelLeft + sidebarWidth + 8;
contentTop = panelTop + headerHeight + 8;
contentWidth = panelWidth - sidebarWidth - 18;
contentHeight = panelHeight - headerHeight - playerHeight - 18;
```

- [ ] **Step 4: Add view-switch helpers**

Add helpers:

```java
private void switchView(int view) {
    activeView = view;
    if (view == VIEW_PLAYLISTS) {
        viewTitle = "My Playlists";
        statusMessage = playList.isEmpty() ? "No playlists loaded" : "Double-click a playlist";
    } else if (view == VIEW_ALBUMS) {
        viewTitle = "Subscribed Albums";
        statusMessage = subList.isEmpty() ? "No albums loaded" : "Double-click an album";
    } else if (view == VIEW_SEARCH) {
        viewTitle = "Search";
        statusMessage = trackList.isEmpty() ? "Type keywords and press Search" : "Double-click a song";
    } else if (view == VIEW_QUEUE) {
        viewTitle = "Queue";
        statusMessage = queue.isEmpty() ? "Queue is empty" : "Double-click a queued track";
        LoadTrackList(queue);
    }
}

private void showTracks(String title, List<TrackContainer> tracks, String emptyText) {
    viewTitle = title;
    activeView = VIEW_TRACKS;
    LoadTrackList(tracks);
    statusMessage = trackList.isEmpty() ? emptyText : "Double-click a song, or add it to queue";
}
```

- [ ] **Step 5: Render the overlay**

In `drawScreen`, render:

```java
drawDefaultBackground();
drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xDD101820);
drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 28, 0xEE18202A);
drawRect(panelLeft, panelTop + panelHeight - 48, panelLeft + panelWidth, panelTop + panelHeight, 0xEE18202A);
drawString(fontRenderer, "NetEase Cloud Music", panelLeft + 10, panelTop + 10, 0xFFFFFF);
drawString(fontRenderer, viewTitle, contentLeft, panelTop + 10, 0xFFFFFF);
```

Draw only the list matching the active view:

```java
if (activeView == VIEW_PLAYLISTS && slotPlayList != null) {
    slotPlayList.drawScreen(mouseX, mouseY, partialTicks);
} else if (activeView == VIEW_ALBUMS && slotSubList != null) {
    slotSubList.drawScreen(mouseX, mouseY, partialTicks);
} else if ((activeView == VIEW_TRACKS || activeView == VIEW_SEARCH || activeView == VIEW_QUEUE) && slotTracks != null) {
    slotTracks.drawScreen(mouseX, mouseY, partialTicks);
}
```

Draw the search field only in search view:

```java
if (activeView == VIEW_SEARCH && searchField != null) {
    searchField.drawTextBox();
}
```

Draw the player bar metadata using trimmed strings:

```java
String title = currentTrack == null ? "No track playing" : currentTrack.getName();
String meta = currentTrack == null ? currentSource : trackMeta(currentTrack);
drawString(fontRenderer, fontRenderer.trimStringToWidth(title, panelWidth - 190), panelLeft + 10, panelTop + panelHeight - 38, 0xFFFFFF);
drawString(fontRenderer, fontRenderer.trimStringToWidth(meta, panelWidth - 190), panelLeft + 10, panelTop + panelHeight - 24, 0xAAAAAA);
```

- [ ] **Step 6: Wire button actions**

Extend `actionPerformed` for navigation, search, queue, previous, next, close, and selected playback:

```java
case BUTTON_NAV_PLAYLISTS:
    switchView(VIEW_PLAYLISTS);
    break;
case BUTTON_NAV_ALBUMS:
    switchView(VIEW_ALBUMS);
    break;
case BUTTON_NAV_SEARCH:
    switchView(VIEW_SEARCH);
    break;
case BUTTON_NAV_QUEUE:
    switchView(VIEW_QUEUE);
    break;
case BUTTON_SEARCH:
    runSearch();
    break;
case BUTTON_ADD_QUEUE:
    addSelectedToQueue();
    break;
case BUTTON_PREVIOUS:
    playPrevious();
    break;
case BUTTON_PLAY_SELECTED:
    PlayMusic();
    break;
case BUTTON_NEXT:
    playNext();
    break;
case BUTTON_CLOSE:
    mc.displayGuiScreen(null);
    break;
```

- [ ] **Step 7: Add queue and search helpers**

Implement:

```java
private void runSearch() {
    String keywords = searchField == null ? "" : searchField.getText();
    if (isBlank(keywords)) {
        statusMessage = "Type keywords and press Search";
        return;
    }
    showTracks("Search: " + keywords.trim(), musicManager.SearchSongs(keywords), "No tracks found");
    activeView = VIEW_SEARCH;
}

private void addSelectedToQueue() {
    if (selectedTrack == null) {
        statusMessage = "Select a song first";
        return;
    }
    queue.add(selectedTrack);
    if (queueIndex < 0) {
        queueIndex = 0;
    }
    statusMessage = "Added to queue: " + selectedTrack.getName();
}

private void playNext() {
    if (queue.isEmpty()) {
        statusMessage = "Queue is empty";
        return;
    }
    queueIndex = queueIndex < 0 ? 0 : (queueIndex + 1) % queue.size();
    selectedTrack = queue.get(queueIndex);
    PlayMusic();
}

private void playPrevious() {
    if (queue.isEmpty()) {
        statusMessage = "Queue is empty";
        return;
    }
    queueIndex = queueIndex <= 0 ? queue.size() - 1 : queueIndex - 1;
    selectedTrack = queue.get(queueIndex);
    PlayMusic();
}
```

- [ ] **Step 8: Handle keyboard and mouse for search**

Add:

```java
@Override
protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (activeView == VIEW_SEARCH && searchField != null && searchField.textboxKeyTyped(typedChar, keyCode)) {
        return;
    }
    if (activeView == VIEW_SEARCH && keyCode == Keyboard.KEY_RETURN) {
        runSearch();
        return;
    }
    super.keyTyped(typedChar, keyCode);
}

@Override
protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    if (searchField != null) {
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }
    super.mouseClicked(mouseX, mouseY, mouseButton);
}
```

- [ ] **Step 9: Run compile check**

Run: `./gradlew compileJava`

Expected: Java compilation succeeds.

## Task 4: Verify Build And Integration

**Files:**
- Verify only.

- [ ] **Step 1: Run full build**

Run: `./gradlew build`

Expected: Build succeeds and reobfuscation runs.

- [ ] **Step 2: Inspect changed files**

Run: `git diff --stat`

Expected: Changes are limited to the planned Java files and plan documentation.

- [ ] **Step 3: Manual runtime smoke test**

Run: `./gradlew runClient`

Expected checks:

- Music box opens the overlay.
- Logged-out state still redirects to QR login.
- Sidebar navigation switches views.
- Playlist and album double-clicks load tracks.
- Search accepts input and returns song rows.
- Double-clicking a track plays through existing server message flow.
- Stop sends a stop message.
- Add queue, previous, and next update the player bar and trigger playback.
