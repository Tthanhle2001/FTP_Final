package bll;

import bll.helpers.DateHelper;
import bll.helpers.Encryptions;
import bll.helpers.FileExtensions;
import bll.helpers.ThreadRandoms;
import dal.Services.*;
import java.util.ArrayList;
import java.util.List;
import middlewares.HandleVerify;
import models.*;

public class UserBLL {
    
    private final UserServices userServices = new UserServices();
    private final FileServices fileServices = new FileServices();
    private final FolderServices folderServices = new FolderServices();
    private final FolderShareServices folderShareServices = new FolderShareServices();
    private final FileShareServices fileShareServices = new FileShareServices();
    private final PermissionServices permissionServices = new PermissionServices();
    
    public UserBLL() {
    }
    
    public List<Users> getAllUser() {
        return userServices.GetAll();
    }
    
    public boolean UpdatePermissionForUser(String email, String perId) {
        return userServices.UpdatePerId(email, perId);
    }
    
    public boolean UpdateFileSizeUpload(String email, long size) {
        return userServices.UpdateCapacity("FileSizeUpload", email, String.valueOf(size));
    }
    
    public boolean UpdateFileSizeDownload(String email, long size) {
        return userServices.UpdateCapacity("FileSizeDownload", email, String.valueOf(size));
    }
    
    public boolean UpdateAnonymousPermission(String email, String per) {
        return userServices.UpdateAnonymousPermission(email, per);
    }
    
    public void genarateAnonymous() {
        if (!folderServices.checkAnonymousExist("anonymous")) {
            String desPath = FileExtensions.getLocalWorkSortPath() + "/src/DATA/anonymous";
            
            Folders folder = new Folders();
            folder.setFolderId("anonymous");
            folder.setFolderName("anonymous");
            folder.setFolderPath(desPath);
            folder.setEmail("anonymous");
            folder.setSize("1073741824");    // 1GB
            folder.setRemainingSize("1073741824"); // 1GB
            folder.setCreateAt(DateHelper.Now());
            folder.setFolderUserPermission("unlock");
            
            FileExtensions.generateFolder(desPath); // tạo folder trong server
            folderServices.Create(folder);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Register">
    public HandleResult registerUser(Users user) {
        user.setPassword(Encryptions.md5(user.getPassword()));
        String prefixEmail = user.getEmail().split("@")[0].toString();
        String desPath = FileExtensions.getLocalWorkSortPath() + "/src/DATA/" + prefixEmail;
        
        Folders folder = new Folders();
        folder.setFolderId(ThreadRandoms.uuid());
        folder.setFolderName(prefixEmail);
        folder.setFolderPath(desPath);
        folder.setEmail(user.getEmail());
        folder.setSize("1073741824");    // 1GB
        folder.setRemainingSize("1073741824"); // 1GB
        folder.setCreateAt(DateHelper.Now());
        
        boolean generateFolder = FileExtensions.generateFolder(desPath); // tạo folder trong server
        boolean createUser = userServices.Create(user);
        boolean createFolder = folderServices.Create(folder);
        if (createUser && generateFolder && createFolder) {
            return new HandleResult(true, "Đăng ký tài khoản thành công!!!");
        }
        return new HandleResult(false, "Đã xảy ra lỗi khi đăng ký.\nVui lòng kiểm tra lại thông tin.!!!");
    }
    // </editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Authenticate with user">
    public HandleResult authenticate(Users user) {
        HandleResult verifyResult = HandleVerify.verifyAuthor(user);
        if (verifyResult.isSuccessed()) {
            Users userInfo = userServices.Find(user.getEmail());
            Folders folderInfo = folderServices.FindFolderParentByEmail(user.getEmail());
            /* trả về client: 
            + trạng thái
            + thông tin user 
            + thông tin thư mục cha
            + danh sách thư mục con
            + danh sách file
            
            NOTE: vì do vấn đề sending qa socket bị giới hạn dung lượng byte 
            nên ta phải tách ra 2 hàm để return Object về cho Client
            * hàm 1: trả về thông tin đơn
            * hàm 2: trả về danh sách file + folder con 
             */
            if (userInfo != null) {
                return new HandleResult(true, verifyResult.getMessage(),
                        ((userInfo != null) ? userInfo : null),
                        folderInfo);
            } else {
                return new HandleResult(false, "Không tìm thấy email này.");
            }
        }
        return verifyResult;
    }
    
    public HandleResult getAuthenData(String folderParentId, String email) {
        // lấy ra danh sách các folder con bậc 1
        List<Folders> listFolderChildInfo = new ArrayList<>();
        listFolderChildInfo = folderServices.FindListChildFolder(folderParentId);
        
        List<Folders> folderGrandChildren = new FolderBLL().getFolderGrandChildren(listFolderChildInfo);
        if (!folderGrandChildren.isEmpty()) {
            listFolderChildInfo.addAll(folderGrandChildren);
        }

        // lấy ra toàn bộ tất cả các file của user + anonymous
        String prexEmail = email.split("@")[0];
        List<Files> listFileInfo = new ArrayList<>();
        listFileInfo = new FileBLL().GetFilesByPrexEmail(prexEmail);
        
        List<Files> listFileAnonymous = new ArrayList<>();
        listFileAnonymous = new FileBLL().GetFilesByPrexEmail("anonymous");

        // thêm toàn bộ file anonymous vào trong list file info
        listFileInfo.addAll(listFileAnonymous);
        
        return new HandleResult(listFolderChildInfo, listFileInfo);
    }
    
    public HandleResult getAuthenDataLockAnonymous(String folderParentId, String email) {
        // lấy ra danh sách các folder con bậc 1
        List<Folders> listFolderChildInfo = new ArrayList<>();
        listFolderChildInfo = folderServices.FindListChildFolder(folderParentId);
        
        List<Folders> folderGrandChildren = new FolderBLL().getFolderGrandChildren(listFolderChildInfo);
        if (!folderGrandChildren.isEmpty()) {
            listFolderChildInfo.addAll(folderGrandChildren);
        }

        // lấy ra toàn bộ tất cả các file của user + anonymous
        String prexEmail = email.split("@")[0];
        List<Files> listFileInfo = new ArrayList<>();
        listFileInfo = new FileBLL().GetFilesByPrexEmail(prexEmail);
        
        return new HandleResult(listFolderChildInfo, listFileInfo);
    }
    
    public HandleResult getAuthenDataShare(String email) {
        
        // List<FileShares> listFileShares = fileShareServices.GetFileShareToMe(email);
        // List<Files> listFiles = new ArrayList<>();
        // for (FileShares item : listFileShares) {
        //     listFiles.add(fileServices.Find(item.getFileId()));
        // }
        List<FileShares> listFileShares = fileShareServices.GetFileShareToMe(email);
        List<Files> listFiles = new ArrayList<>();

        for (FileShares item : listFileShares) {
            Files file = fileServices.Find(item.getFileId());
            if (file != null) {
                file.setEmailShare(item.getFromEmail()); // 👈 Gán chủ sở hữu
                listFiles.add(file);
            }
        }

        List<FolderShares> listFolderShare = folderShareServices.getFolderShareToMe(email);
        List<Folders> listFolders = new ArrayList<>();
        for (FolderShares item : listFolderShare) {
            listFolders.add(folderServices.Find(item.getFolderId()));
        }
        
        List<Permissions> listPermission = permissionServices.GetAll();
        
        return new HandleResult(listFileShares, listFolderShare, listFiles, listFolders, listPermission);
    }

    // </editor-fold>
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Authenticate with anonymous permission">
    public HandleResult authenticateWithAnonymousPermission(String anonymous) {
        Users userInfo = new Users();
        userInfo.setFullName(anonymous);
        userInfo.setEmail(anonymous);
        userInfo.setPermissionId("all");
        userInfo.setFileSizeUpload("1073741824");   // max upload anonymous 1GB
        userInfo.setFileSizeDownload("1073741824");   // max download anonymous 1GB
        userInfo.setAnonymousPermission("unlock");
        
        Folders folderInfo = new Folders();
        folderInfo.setFolderId(anonymous);
        folderInfo.setFolderName(anonymous);
        folderInfo.setEmail(anonymous);
        folderInfo.setFolderPath(FileExtensions.getLocalWorkSortPath() + "/src/DATA/" + anonymous);
        folderInfo.setFolderUserPermission("unlock");
        
        folderInfo.setSize("1073741824");    // 1GB
        folderInfo.setRemainingSize("1073741824"); // 1GB


        return new HandleResult(true, "Welcome to FTP Service with anonymous permission.!!!",
                userInfo, folderInfo);
    }
    
    public HandleResult getAuthenDataWithAnonymousPermission() {
        // lấy ra danh sách các folder con
        List<Files> listFileAnonymous = new FileBLL().GetFilesByPrexEmail("anonymous");
        
        return new HandleResult(null, listFileAnonymous);
    }
    // </editor-fold>
}
