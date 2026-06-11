# Minecraft Music Player Overlay Design

## Goal

Replace the current simple NetEase Cloud Music panel with a Minecraft-style floating music player. The player should stay compact enough for in-game use while providing the core structure expected from a real music player: navigation, library browsing, search, current-track display, playback controls, queue management, and clear status feedback.

## Scope

This change focuses on the client GUI and NetEase API client surface. Existing playback synchronization should remain based on `MusicMessage` and `EnumMusicCommand`, so server-side playback behavior does not need a protocol redesign.

Included:

- A single floating `MusicPannel` with multiple internal views.
- Left navigation for Library, Albums, Search, Private FM, and Queue.
- Content views for playlist list, album list, playlist or album tracks, search results, and queue.
- A bottom playback bar with current track metadata and primary controls.
- Client-side playback queue with previous and next navigation.
- NetEase search API support for song search.
- Loading, empty, and error state text for each view.

Not included in the first implementation:

- Lyrics.
- Ranking charts.
- Daily recommendations.
- Persistent queue storage.
- New server packet commands beyond the current play and stop flow.

## UI Structure

The screen remains a game overlay, not a full desktop-style application. It should render a centered, semi-transparent panel over the game background.

Panel regions:

- Header: title, login or source status, refresh button, close button.
- Sidebar: navigation entries for playlists, albums, search, private FM, and queue.
- Content: view-specific list or detail content.
- Player bar: current track title, artist and album, source label, and playback buttons.

The interface should use Minecraft GUI primitives and simple shapes instead of large new image assets. The existing background texture may be kept as subtle ambience, but the main readability should come from drawn rectangles, section labels, selected states, and trimmed text.

## Navigation Model

`MusicPannel` owns an internal view state instead of opening a new `GuiScreen` for every level.

Primary views:

- Playlists: shows user playlists.
- Albums: shows subscribed albums.
- Search: includes a text input and search results.
- Queue: shows the current client-side queue.
- Tracks: shows tracks loaded from a selected playlist, album, or search result context.

Transitions:

- Selecting Playlists or Albums displays the top-level list.
- Double-clicking a playlist or album loads tracks and changes to Tracks.
- Running a search updates the Search view results.
- Double-clicking a track plays it immediately.
- A dedicated action adds a selected track to the queue.
- Queue next and previous controls move through the queue and play the target track.

## Playback Behavior

The existing play command remains the authoritative playback path:

1. The user selects a track or queue item.
2. `MusicPannel` asks `IMusicManager.GetMusicById` for the stream URL.
3. It builds `MusicInfoWrapper` with `EnumMusicCommand.PLAY`.
4. It sends `MusicMessage` to the server through `MusicPacketHandler`.

Stop behavior remains `EnumMusicCommand.STOP`.

The panel should keep local current-track metadata so the player bar can show what the user last requested. This metadata is UI state only; it does not imply that playback is globally synchronized beyond the existing message flow.

## NetEase API Additions

Add a search method to `IMusicManager`:

```java
ArrayList<TrackContainer> SearchSongs(String keywords);
```

Implement it in `NeteaseCloudMusicManager` using the NetEase-compatible `/search` endpoint with song type. Returned results should populate `TrackContainer` with id, name, artist, and album when available.

The manager should fail safely:

- Return an empty list on missing login, blank keyword, request failure, or malformed response.
- Avoid throwing API parsing errors into GUI code.
- Reuse existing `safeString` and blank checking style.

## Error And Empty States

Every view should render a useful state instead of appearing blank:

- Not loaded: "Select a playlist or album".
- Loading failed: "Load failed, try refresh".
- Empty result: "No tracks found".
- Search prompt: "Type keywords and press Search".
- Queue empty: "Queue is empty".

Network work currently happens synchronously in the existing panel. The first implementation may keep this pattern to limit risk, but the UI should centralize state messages so async loading can be added later without changing view structure.

## Component Boundaries

Keep changes local and compatible with Forge 1.12.2 Java 8:

- `MusicPannel`: screen state, layout, view switching, button actions, text input, queue operations.
- `GuiSlotPlayList`: render playlist entries using bounds supplied by `MusicPannel`.
- `GuiSlotSubList`: render album entries using bounds supplied by `MusicPannel`.
- `GuiSlotTracks`: render track entries with title and optional artist or album.
- `IMusicManager`: add search method.
- `NeteaseCloudMusicManager`: implement search and add DTO parsing if needed.

Avoid introducing external UI libraries or changing server-safe classes to import client-only APIs.

## Validation

Minimum verification:

- `./gradlew build`
- Open the panel with the music box.
- Unauthenticated users still go to QR login.
- Playlists and albums load.
- Double-clicking a playlist or album loads tracks.
- Search returns song results for a normal keyword.
- Double-clicking a track sends play command.
- Stop sends stop command.
- Queue add, previous, and next update current-track UI and play through the existing message path.
- Empty and failure states render readable text.
