package team.info.ncmfm.interfaces;

import team.info.ncmfm.entity.PersonalFM;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.QrStatus;
import team.info.ncmfm.model.SubListContainer;
import team.info.ncmfm.model.TrackContainer;

import java.util.ArrayList;

public interface IMusicManager {
    ArrayList<PlayListContainer> LoadPlayList();

    ArrayList<TrackContainer> LoadTrackList(long id);

    ArrayList<TrackContainer> LoadAlbumTrackList(long id);

    ArrayList<SubListContainer> LoadSubList();

    String GetMusicById(long id);

    void updateLoginState();

    void login();

    /**
     * 是否已登录（缓存中存在有效用户）
     */
    boolean isLoggedIn();

    /**
     * 创建登录二维码，返回二维码图片（base64 PNG，形如 data:image/png;base64,...）。
     * 失败返回 null。内部会记录本次二维码的 unikey 供 {@link #checkQrStatus()} 轮询。
     */
    String createQrCode();

    /**
     * 轮询当前二维码扫码状态。当返回 {@link QrStatus#CONFIRMED} 时，登录已完成。
     */
    QrStatus checkQrStatus();

    /**
     * 私人FM
     * @Return: team.info.ncmfm.entity.PersonalFM
     * @Author: FOXCELL
     * @Date: 2020/11/23 9:55
     */
    PersonalFM personalFm();
}
