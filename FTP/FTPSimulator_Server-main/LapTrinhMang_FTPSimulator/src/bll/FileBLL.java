package bll;

import bll.helpers.DateHelper;
import dal.Services.FileServices;
import dal.Services.FileShareServices;
import dal.Services.FolderServices;
import dal.Services.FolderShareServices;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import models.FileShares;
import models.Files;
import models.FolderShares;
import java.nio.file.*;
import javax.imageio.ImageIO;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
public class FileBLL {

    private final FileShareServices fileShareServices = new FileShareServices();
    private final FileServices fileService = new FileServices();

    public FileBLL() {
    }

    /* Ý tưởng chức năng upload insert file:
        khi upload fie => kiểm tra tên file kèm đường dẫn đc upload đã tồn tại trong db chưa
    => rồi: - lấy ra dung lượng còn lại của folder từ folderId đc gửi đính kèm lên
            - CT tính: Cộng dung lượng file cũ vào dung lượng còn của folder rồi mới Trừ đi dung lượng file mới gửi lên
            - tiến hành update dung lượng cho folder của user
            - tiến hành update lại thông tin file
    => chưa: - lấy ra dung lượng còn lại của folder từ folderId đc gửi đính kèm lên
             - CT tính: lấy dung lượng còn của folder Trừ đi dung lượng file mới gửi lên
             - tiến hành update dung lượng cho folder của user
             - tiến hành thêm mới thông tin file vào database
    
    * kiểm tra folder mà user upload file lên có dc chia sẻ cho ai chưa
    ** nếu có -> chia sẻ file đó vs những ng đc chia sẻ trong folder đó, quyền sẽ lấy theo quyền truy cập folder đc gán sẵn của ng đó
     */
    public boolean insertNewFile(Files file) {
        String remainingSizeFolder = new FolderServices().GetRemainingSizeFolder(file.getFolderId());
        Files fileExist = fileService.FindByNameAndSourcePath(file.getFileName(), file.getSourcePath());
        if (fileExist != null) {
            double remainingSizeFolderConvert
                    = Double.parseDouble(remainingSizeFolder.replaceAll(",", ""))
                    + Double.parseDouble(fileExist.getFileSize().replaceAll(",", ""))
                    - Double.parseDouble(file.getFileSize().replaceAll(",", ""));
            try {
                new FolderServices().
                        UpdateRemainingSize(String.valueOf(remainingSizeFolderConvert), file.getFolderId());
            } catch (Exception e) {
                System.err.println("Xảy ra lỗi khi update dung lượng còn lại của folder" + e);
            }
            return fileService.UpdateFileIsExist(file);
        }

        double remainingSizeFolderConvert
                = Double.parseDouble(remainingSizeFolder.replaceAll(",", ""))
                - Double.parseDouble(file.getFileSize().replaceAll(",", ""));
        try {
            new FolderServices().
                    UpdateRemainingSize(String.valueOf(remainingSizeFolderConvert), file.getFolderId());
        } catch (Exception e) {
            System.err.println("Xảy ra lỗi khi update dung lượng còn lại của folder" + e);
        }

        // kiểm tra folder mà user upload file lên có dc chia sẻ cho ai chưa
        List<FolderShares> folderShareses = new FolderShareServices().getListFolderShared(file.getFolderId());
        if (!folderShareses.isEmpty()) {
            for (FolderShares folderShares : folderShareses) {
                fileShareServices.Create(new FileShares(file.getFileId(),
                        folderShares.getFromEmail(), folderShares.getToEmail(), folderShares.getPermissionId(), DateHelper.Now()));
            }
        }
        return fileService.Create(file);
    }

    public boolean insertNewFileShare(Files file) {
        String remainingSizeFolder = new FolderServices().GetRemainingSizeFolder(file.getFolderId());
        Files fileExist = fileService.FindByNameAndSourcePath(file.getFileName(), file.getSourcePath());
        if (fileExist != null) {
            double remainingSizeFolderConvert
                    = Double.parseDouble(remainingSizeFolder.replaceAll(",", ""))
                    + Double.parseDouble(fileExist.getFileSize().replaceAll(",", ""))
                    - Double.parseDouble(file.getFileSize().replaceAll(",", ""));
            try {
                new FolderServices().
                        UpdateRemainingSize(String.valueOf(remainingSizeFolderConvert), file.getFolderId());
            } catch (Exception e) {
                System.err.println("Xảy ra lỗi khi update dung lượng còn lại của folder" + e);
            }
            return fileService.UpdateFileIsExist(file);
        }
        
        double remainingSizeFolderConvert
                = Double.parseDouble(remainingSizeFolder.replaceAll(",", ""))
                - Double.parseDouble(file.getFileSize().replaceAll(",", ""));
        try {
            new FolderServices().
                    UpdateRemainingSize(String.valueOf(remainingSizeFolderConvert), file.getFolderId());
        } catch (Exception e) {
            System.err.println("Xảy ra lỗi khi update dung lượng còn lại của folder" + e);
        }
        fileShareServices.Create(new FileShares(file.getFileId(), file.getEmailShare(), file.getEmailShare(), "a", DateHelper.Now()));
        return fileService.Create(file);
    }

    public List<Files> GetFilesByFolderId(String folderId) {
        return fileService.GetListFileByFolderId(folderId.trim());
    }

    public List<Files> GetFilesByPrexEmail(String prexEmail) {
        return fileService.GetListFileByPrexEmail(prexEmail.trim());
    }
    
    // public String previewFile(Files file) {
    //     Path path = Paths.get(file.getSourcePath());
    //     try {
    //         return java.nio.file.Files.readString(path, StandardCharsets.UTF_8); // 👈 Gọi đúng class
    //     } catch (IOException e) {
    //         return "Không thể đọc file văn bản.";
    //     }
    // }

    // public byte[] previewImage(Files file) {
    //     try {
    //         return java.nio.file.Files.readAllBytes(Paths.get(file.getSourcePath())); // 👈 Gọi đúng class
    //     } catch (IOException e) {
    //         return null;
    //     }
    // }


    public String previewFile(Files file) {
        Path path = Paths.get(file.getSourcePath(), file.getFileName());
        System.out.println("[previewFile] Đọc file văn bản từ: " + path);

        try {
            return java.nio.file.Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[previewFile] Lỗi đọc file: " + e.getMessage());
            return "Không thể đọc file văn bản.";
        }
    }

    public byte[] previewImage(Files file) {
        try{
            Path path = Paths.get(file.getSourcePath(), file.getFileName());
            BufferedImage bufferedImage = ImageIO.read(path.toFile());
            if (bufferedImage == null){
                System.out.println("Ảnh null hoặc không hợp lệ");
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            return baos.toByteArray();
        }catch(IOException e){
            System.err.println("Lỗi đọc hoặc nén ảnh "+ e.getMessage());
            return null;
        }
    }

    public String previewDocFile(Files file) {
        Path path = Paths.get(file.getSourcePath(), file.getFileName());
        String fileName = file.getFileName().toLowerCase();

        try {
            if (fileName.endsWith(".pdf")) {
                System.out.println("Đang đọc file PDF: " + path);
                try (PDDocument document = PDDocument.load(path.toFile())) {
                    if (document.isEncrypted()) {
                        return "Không thể xem PDF vì file có mã hóa.";
                    }
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);
                    if(text.trim().isEmpty()){
                        return "File pdf không chứa văn bản, có thể là dạng ảnh Scan";
                    }
                    return text;
                }
            } else if (fileName.endsWith(".docx")) {
                System.out.println("Đang đọc file DOCX: " + path);
                try (FileInputStream fis = new FileInputStream(path.toFile())) {
                    XWPFDocument doc = new XWPFDocument(fis);
                    XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                    return extractor.getText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Lỗi đọc file: " + e.getMessage();
        }

        return "Không hỗ trợ định dạng này.";
    }


}

