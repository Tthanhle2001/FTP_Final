package components;

import bll.FileBLL;
import bll.FolderBLL;
import bll.SharesBLL;
import bll.UserBLL;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import middlewares.HandleVerify;
import models.DataShare;
import models.FileDownloadInfo;
import models.FileEvent;
import models.Files;
import models.Folders;
import models.HandleResult;
import models.MembersOnline;
import models.ObjectRequest;
import models.Users;

public class ListenThread extends Thread {

    private Socket clientSocket;
    private String socketId;
    private boolean ANONYMOUS_PERMISSION = true;
    private BufferedWriter out;
    private BufferedReader in;
    private boolean isDisconnect = false;

    private ObjectInputStream objInputStream;
    private ObjectOutputStream objOutputStream;
    private File dstFile;
    private FileOutputStream fileOutputStream;
    private MembersOnline member;
    private ObjectRequest request;

    public ListenThread(Socket clientSocket, String id) {
        this.clientSocket = clientSocket;
        this.socketId = id;
    }

    public Socket getSocket() {
        return clientSocket;
    }

    public String getSocketId() {
        return socketId;
    }

    public boolean isANONYMOUS_PERMISSION() {
        return ANONYMOUS_PERMISSION;
    }

    public void setANONYMOUS_PERMISSION(boolean ANONYMOUS_PERMISSION) {
        this.ANONYMOUS_PERMISSION = ANONYMOUS_PERMISSION;
    }

    public MembersOnline getMember() {
        return member;
    }

    public void setMember(MembersOnline memberInfo) {
        member = memberInfo;
    }

    public static ArrayList<ListenThread> getListClient() {
        return Server.getListClient();
    }

    // <editor-fold defaultstate="collapsed" desc="Xử lý gửi - nhận file phía Server (không dùng trong đây)">
    public void saveFile() {
        try {
            // Bước 1: đọc đối tượng truyền từ client qua
            FileEvent fileEvent = (FileEvent) objInputStream.readObject();

            // Bước 2: Kiểm tra trạng thái có bị "Error" ko???
            if (fileEvent.getStatus().equalsIgnoreCase("Error")) {

                // Nếu có: Thông báo lỗi => Chấm dứt chương trình
                System.err.println("Xảy ra lỗi..");
            }

            // OK => 
            // kiểm tra đường dẫn đích tồn tại chưa
            if (!new File(fileEvent.getDestinationDirectory()).exists()) {
                // Nếu chưa => tạo mới đường dẫn
                new File(fileEvent.getDestinationDirectory()).mkdirs();
            }

            // tạo file
            String outputFile = fileEvent.getDestinationDirectory() + fileEvent.getFilename();
            dstFile = new File(outputFile);

            // FileOutputStream dùng để ghi dữ liệu vào file theo định dạng byte
            fileOutputStream = new FileOutputStream(dstFile);
            fileOutputStream.write(fileEvent.getFileData());    // bđ ghi vô luồng
            fileOutputStream.flush();   // đẩy luồng ghi vô file
            fileOutputStream.close();   // đóng quá trình ghi sau khi hoàn tất

            System.out.println("Output file : " + outputFile + " được ghi thành công. ");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String sourceFilePath, String destinationPath) {

        // tạo đối tượng FileEvent
        FileEvent fileEvent = new FileEvent();

        // lấy ra tên file
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1, sourceFilePath.length());

        fileEvent.setFilename(fileName);
        fileEvent.setSourceDirectory(sourceFilePath);
        fileEvent.setDestinationDirectory(destinationPath);

        File file = new File(sourceFilePath);
        if (file.isFile()) {
            try {
                // DataInputStream đọc dữ liệu nguyên thủy từ luồng đầu vào ( ở đây là file đầu vào)
                DataInputStream diStream = new DataInputStream(new FileInputStream(file));
                long len = (int) file.length();
                byte[] fileBytes = new byte[(int) len];
                int read = 0;
                int numRead = 0;
                while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
                    read = read + numRead;
                }

                fileEvent.setFileSize(len);
                fileEvent.setFileData(fileBytes);
                fileEvent.setStatus("Success");
            } catch (Exception e) {
                e.printStackTrace();
                fileEvent.setStatus("Error");
            }
        } else {
            System.out.println("Không tìm thấy tệp ở vị trí đã chỉ định.");
            fileEvent.setStatus("Error");
        }

        //Ghi đối tượng tệp vào thư mục
        try {
            objOutputStream.writeObject(fileEvent);
            System.out.println("Đang gửi file...");
            Thread.sleep(3000);
            System.out.println("Hoàn tất!");
            objOutputStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // </editor-fold> 
//    public Files FileSaver() throws IOException, ClassNotFoundException {
//        new FileHandler().ServerSaver(objInputStream);
//
//        Files file = (Files) objInputStream.readObject();
//        return file;
//    }
    public void saveFile(FileEvent fileEvent) {
        try {

            // Bước 2: Kiểm tra trạng thái có bị "Error" ko???
            if (fileEvent.getStatus().equalsIgnoreCase("Error")) {

                // Nếu có: Thông báo lỗi => Chấm dứt chương trình
                System.err.println("Xảy ra lỗi..");
            }

            // OK => 
            // kiểm tra đường dẫn đích tồn tại chưa
            if (!new File(fileEvent.getDestinationDirectory()).exists()) {
                // Nếu chưa => tạo mới đường dẫn
                new File(fileEvent.getDestinationDirectory()).mkdirs();
            }

            // tạo file
            String outputFile = fileEvent.getDestinationDirectory() + fileEvent.getFilename();
            dstFile = new File(outputFile);

            // FileOutputStream dùng để ghi dữ liệu vào file theo định dạng byte
            fileOutputStream = new FileOutputStream(dstFile);
            fileOutputStream.write(fileEvent.getFileData());    // bđ ghi vô luồng
            fileOutputStream.flush();   // đẩy luồng ghi vô file
            fileOutputStream.close();   // đóng quá trình ghi sau khi hoàn tất

            System.out.println("Output file : " + outputFile + " được ghi thành công. ");

        } catch (IOException ex) {
            System.err.println("Lỗi khi lưu file");
        }
    }

    public void responseDownloadFile(String message, FileDownloadInfo fileDownloadInfo) {
        File file = new File(fileDownloadInfo.getSourceFilePath() + "/" + fileDownloadInfo.getFileName());

        // tạo đối tượng FileEvent
        FileEvent fileEvent = new FileEvent();

        fileEvent.setFilename(fileDownloadInfo.getFileName());
        fileEvent.setDestinationDirectory(fileDownloadInfo.getDestinationPath());

        if (file.isFile()) {
            try {
                // DataInputStream đọc dữ liệu nguyên thủy từ luồng đầu vào ( ở đây là file đầu vào)
                DataInputStream diStream = new DataInputStream(new FileInputStream(file));
                long len = (int) file.length();
                byte[] fileBytes = new byte[(int) len];
                int read = 0;
                int numRead = 0;
                while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
                    read = read + numRead;
                }

                fileEvent.setFileSize(len);
                fileEvent.setFileData(fileBytes);
                fileEvent.setStatus("Success");
                System.out.println("Download file xuống thành công");
            } catch (IOException ex) {
                System.err.println("Download file xuống lên thất bại" + ex);
                fileEvent.setStatus("Error");
            }
        } else {
            System.out.println("Không tìm thấy tệp ở vị trí đã chỉ định.");
            fileEvent.setStatus("Error");
        }
        try {
            objOutputStream.writeObject(new ObjectRequest(message, fileEvent));
            objOutputStream.flush();
            objOutputStream.reset();
        } catch (IOException ex) {
            Logger.getLogger(ListenThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendMessage(String message) {
        try {
            out.write(message + "\n");
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(ListenThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void accept_disconnect() {
        try {
            isDisconnect = true;
            objOutputStream.writeObject(new ObjectRequest("ACCCEP_DISCONNECT"));
        } catch (IOException ex) {
            System.err.println("Server xảy ra lỗi IOException khi chấp nhận client DISCONNECT - " + ex);
        }
    }

    public String getMessage() {
        String message;
        try {
            message = in.readLine().replace("\n", "").replace("\r", "");
            if (String.valueOf(message.charAt(0)).equalsIgnoreCase("y")) {
                message = message.substring(1, message.length());
            }
        } catch (IOException ex) {
            System.err.println("lỗi ở đây getMessage - " + ex);
            message = "DISCONNECT";
        }
        return message;
    }

//------------------------------------------------------------------------
    public void registerMemberOnline(Users user) {
        MembersOnline memberInfo = new MembersOnline(this, user);
        setMember(memberInfo);
        Server.registerMemberOnline(memberInfo);
    }

    public void removeMemberDisconnect(MembersOnline member) {
        Server.removeMemberDisconnect(member);
    }

    public ArrayList<MembersOnline> getMembersOnline() {
        return Server.getMembersOnline();
    }
//------------------------------------------------------------------------

//    public void notification(String id) {
//        for (ListenThread client : getListClient()) {
//            if (client.getSocketId().equals(id)) {
//                client.sendMessage("chào client " + id);
//            }
//        }
//    }
    public void notification(String email) {
        for (MembersOnline member : getMembersOnline()) {
            if (member.getUsers().getEmail().trim().equals(email.trim())) {
                member.getListenThread().response("response_notification", "Chào bạn");
                break;
            }
        }
    }

    public void notificationShared(List<String> listEmailShared, String message) {
        for (String emailShared : listEmailShared) {
            for (MembersOnline member : getMembersOnline()) {
                if (member.getUsers().getEmail().trim().equals(emailShared.trim())) {
                    member.getListenThread().response("notifi_shareddata", message);
                }
            }
        }

    }

    public void response(String data) {
        try {
            out.write(data);
            out.newLine();
            out.flush();
        } catch (IOException ex) {
            System.err.println("Server xảy ra lỗi function response IOException - " + ex);
        }
    }

    // bắn message + Object
    public void response(String message, Object object) {
        try {
            objOutputStream.writeObject(new ObjectRequest(message, object));
            objOutputStream.flush();
        } catch (IOException ex) {
            System.err.println("Server xảy ra lỗi function response IOException - " + ex);
        }
    }

    // Gửi chỉ object HandleResult đơn luồng
    public void responseHandleResult(HandleResult result) {
        try {
            objOutputStream.writeObject(result);
            objOutputStream.flush();
            objOutputStream.reset();
        } catch (IOException ex) {
            System.err.println("Server xảy ra lỗi function responseHandleResult IOException - " + ex);
        }
    }

    @Override
    public void run() {
        System.out.println("Một client id: " + getSocketId() + " vừa kết nối - [Thông tin] " + this.clientSocket);
        try {
            out = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            objOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
            objInputStream = new ObjectInputStream(this.clientSocket.getInputStream());

            // add client connect vô danh sách
            Server.registerClient(this);

            // reload lại Server UI khi client connect
            ServerUI.reloadClientConnect(this);

            while (!isDisconnect) {

                try {

                    // đồng bộ read object
                    synchronized (objInputStream) {
                        request = (ObjectRequest) objInputStream.readObject();
                    }

                    String message = request.getMessage();
                    switch (message.toUpperCase()) {

                        // <editor-fold defaultstate="collapsed" desc="SIGNOUT">
                        case "SIGNOUT": {
                            // Xóa user đã offline khỏi ds user đang online
                            removeMemberDisconnect(getMember());
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="DISCONNECT">
                        case "DISCONNECT": {
                            System.err.println("Client with port " + clientSocket.getPort() + " with disconnect");
                            accept_disconnect();
                            Thread.sleep(3000);

                            try {
                                // Xóa user đã offline khỏi ds user đang online
                                removeMemberDisconnect(getMember());
                            } catch (Exception ex) {
                            }

                            // Xóa client bị đóng kết nối khỏi ds client đang connect
                            Server.removeClientDisconnect(this);

                            // reload lại Server UI khi client disconnect
                            ServerUI.reloadClientDisconnect(this);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="DOWNLOAD_FILE">
                        case "DOWNLOAD_FILE": {
                            System.out.println("Client đòi " + message);
                            FileDownloadInfo fileDownloadInfo = (FileDownloadInfo) request.getObject();
                            responseDownloadFile("accept_download_file", fileDownloadInfo);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="UPLOAD_FILE">
                        case "UPLOAD_FILE": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);
                            Files filesInfo = (Files) request.getObject();
                            FileEvent fileEvent = (FileEvent) request.getFileUpload();

                            new FileBLL().insertNewFile(filesInfo);

                            // convert byte sang file -> lại file lưu trên server
                            saveFile(fileEvent);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="UPLOAD_FILE_SHARE">
                        case "UPLOAD_FILE_SHARE": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);
                            Files filesInfo = (Files) request.getObject();
                            FileEvent fileEvent = (FileEvent) request.getFileUpload();
                            new FileBLL().insertNewFileShare(filesInfo);
                            saveFile(fileEvent);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="NOTIFICATION">
                        // bắn thông báo về cho client bằng id or email
                        case "NOTIFICATION": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);
                            String email = (String) request.getObject();
                            notification(email);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="VERIFY_REGISTER">
                        // CASE ĐĂNG KÝ
                        case "VERIFY_REGISTER": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);
                            Users user = (Users) request.getObject();
                            HandleResult result = HandleVerify.verifyRegisterUser(user);
                            response("response_verify_register", result);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="REGISTER">
                        case "REGISTER": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);

                            Users user = (Users) request.getObject();
                            HandleResult result = new UserBLL().registerUser(user);
                            response("response_register", result);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="AUTHENTICATE">
                        // CASE LOGIN
                        case "AUTHENTICATE": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);

                            Users user = (Users) request.getObject();
                            UserBLL userBLL = new UserBLL();
                            HandleResult result = userBLL.authenticate(user);
                            response("response_authenticate", result);
                            if (result.isSuccessed()) {
                                // user ko bị lock quyền anonymous
                                if (result.getUser().getAnonymousPermission().trim().equals("unlock")) {
                                    responseHandleResult(userBLL
                                            .getAuthenData(result.getFolder().getFolderId(), result.getUser().getEmail()));
                                    responseHandleResult(userBLL.getAuthenDataShare(result.getUser().getEmail()));

                                    // khi user login thành công -> add user vào ds user đang online
                                    registerMemberOnline(result.getUser());
                                } else {    // user bị lock quyền anonymous
                                    responseHandleResult(userBLL
                                            .getAuthenDataLockAnonymous(result.getFolder().getFolderId(), result.getUser().getEmail()));
                                    responseHandleResult(userBLL.getAuthenDataShare(result.getUser().getEmail()));

                                    // khi user login thành công -> add user vào ds user đang online
                                    registerMemberOnline(result.getUser());
                                }
                            }
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="AUTHENTICATE_ANONYMOUS_PERMISSION">
                        case "AUTHENTICATE_ANONYMOUS_PERMISSION": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);

                            String anonymousUser = (String) request.getObject();
                            HandleResult result = new UserBLL()
                                    .authenticateWithAnonymousPermission(anonymousUser);
                            response("response_authenticate_anonymous", result);
                            if (result.isSuccessed()) {
                                responseHandleResult(new UserBLL()
                                        .getAuthenDataWithAnonymousPermission());
                            }
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="RELOAD_WITH_AUTHENTICATE">
                        // CASE RELOAD WITH USER AUTHENTICATE
                        case "RELOAD_WITH_AUTHENTICATE": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);

                            Users user = (Users) request.getObject();
                            UserBLL userBLL = new UserBLL();
                            HandleResult result = userBLL.authenticate(user);
                            response("response_authenticate", result);
                            if (result.isSuccessed()) {
                                // user ko bị lock quyền anonymous
                                if (result.getUser().getAnonymousPermission().trim().equals("unlock")) {
                                    responseHandleResult(userBLL
                                            .getAuthenData(result.getFolder().getFolderId(), result.getUser().getEmail()));
                                    responseHandleResult(userBLL.getAuthenDataShare(result.getUser().getEmail()));

                                } else {    // user bị lock quyền anonymous
                                    responseHandleResult(userBLL
                                            .getAuthenDataLockAnonymous(result.getFolder().getFolderId(), result.getUser().getEmail()));
                                    responseHandleResult(userBLL.getAuthenDataShare(result.getUser().getEmail()));

                                }
                            }
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="RELOAD_WITH_ANONYMOUS">
                        case "RELOAD_WITH_ANONYMOUS": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);

                            String anonymousUser = (String) request.getObject();
                            HandleResult result = new UserBLL()
                                    .authenticateWithAnonymousPermission(anonymousUser);
                            response("response_authenticate_anonymous", result);
                            if (result.isSuccessed()) {
                                responseHandleResult(new UserBLL()
                                        .getAuthenDataWithAnonymousPermission());
                            }
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="NEW_FOLDER">
                        // CASE TẠO FOLDER CON MỚI
                        case "NEW_FOLDER": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);
                            Folders folder = (Folders) request.getObject();
                            new FolderBLL().generateFolderChild(folder);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="SHARE_FILES">
                        case "SHARE_FILES": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);
                            DataShare dataShare = (DataShare) request.getObject();
                            new SharesBLL().shareFiles(dataShare);
                            String notifiShareFile = "Bạn vừa được chia sẻ một tệp tin từ email " + dataShare.getFromEmail();
                            notificationShared(dataShare.getListUserShare(), notifiShareFile);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="SHARE_FOLDER">
                        case "SHARE_FOLDER": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);
                            DataShare dataShare = (DataShare) request.getObject();
//                            new SharesBLL().shareFolder(dataShare);
                            new SharesBLL().shareFolder_Dequy(dataShare);
                            String notifiShareFolder = "Bạn vừa được chia sẻ một thư mục từ email " + dataShare.getFromEmail();
                            notificationShared(dataShare.getListUserShare(), notifiShareFolder);
                            break;
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="FETCH_USERS">
                        case "FETCH_USERS": {
                            System.out.println("Client[port " + getSocket().getPort() + "] said: " + message);
                            String authenEmail = (String) request.getObject();
                            List<Users> listUsers = new UserBLL().getAllUser();
                            response("fetch_users_success", new HandleResult(listUsers));
                            break;
                        }
                        

                        // </editor-fold> 
                                       
                        // <editor-fold defaultstate="collapsed" desc="Preview File">
                        case "PREVIEW_FILE": {
                            Files file = (Files) request.getObject();
                            System.out.println("📥 Server nhận yêu cầu PREVIEW_FILE với path: " + file.getSourcePath()); // thêm log
                            String content = new FileBLL().previewFile(file);
                            response("RESPONSE_PREVIEW", content);
                            break;
                        }
                        // </editor-fold> 
                        
                        // <editor-fold defaultstate="collapsed" desc="Preview IMAGE">
                        case "PREVIEW_IMAGE": {
                            Files file = (Files) request.getObject();
                            System.out.println("📥 Server nhận yêu cầu PREVIEW_IMAGE cho: " + file.getFileName());
                            byte[] imageData = new FileBLL().previewImage(file);
                            response("RESPONSE_PREVIEW_IMAGE", imageData);
                            break;
                        }
                        // </editor-fold> 
              
                        // <editor-fold defaultstate="collapsed" desc="Preview DOC">
                        case "PREVIEW_DOC": {
                            Files file = (Files) request.getObject();
                            System.out.println("📄 Server xử lý PREVIEW_DOC: " + file.getFileName());
                            String content = new FileBLL().previewDocFile(file);
                            response("RESPONSE_PREVIEW", content);
                            break;
                        }
                        // </editor-fold>


                        default: {
                            System.out.println("Client[port " + getSocket().getPort() + "] said default: " + message);
                            response("test", "test");
                            break;
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ListenThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (InterruptedException ex) {
            System.err.println("Server error InterruptedException - " + ex);
        } catch (IOException ex) {
            System.err.println("Server error IOException - " + ex);
        } finally {
            // Xóa client bị đóng kết nối khỏi ds client đang connect
            Server.removeClientDisconnect(this);

            // reload lại Server UI khi client disconnect
            ServerUI.reloadClientDisconnect(this);
        }
    }
}
