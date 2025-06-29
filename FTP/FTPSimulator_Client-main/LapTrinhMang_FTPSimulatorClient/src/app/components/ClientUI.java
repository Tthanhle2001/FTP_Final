package app.components;

import app.components.Modals.CreateNewFolder;
import app.components.Modals.Notification;
import app.components.Modals.SharePeople;
import app.ProcessHandle.ClientThread;
import app.components.Loading.LoadingDownloadFile;
import app.components.Loading.LoadingProcess;
import app.components.Loading.LoadingUploadFile;
import models.FileDownloadInfo;
import models.Permissions;
import models.Files;
import models.Folders;
import models.Users;
import models.FolderShares;
import models.FileShares;
import features.utilities.DateHelper;
import features.utilities.Encryptions;
import features.utilities.FileExtensions;
import features.utilities.ThreadRandoms;
import features.handlers.FileHandler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import org.jsoup.Connection.Request;


public class ClientUI extends javax.swing.JFrame {

    private static boolean processHandler = true;
    private static boolean statusResult = false;
    private static String messageResult = "";

    // code for register handle
    private static boolean isVerify = false;
    private static int verifyCode;
    private static String timestamp = "";

    // return info when after login
    public static Users userInfo;
    public static Folders folderInfo;
    public static Folders folderSelected;
    public static List<Folders> listFolderChildInfo;
    private static List<Files> listFileInfo;
    private static List<FileShares> listFileSharedInfo;
    private static List<FolderShares> listFolderSharedInfo;
    private static List<Files> listFileShareInfo;
    private static List<Folders> listFolderShareInfo;
    private static List<Permissions> listPermissionInfo;

    // list notification
    public static List<String> notifications = new ArrayList<>();
    public static int TOTAL_NOTIFICATIONS = 0;

    // email info
    private static String prexEmailInfo;

    // chế độ upload file
    private static boolean IS_UPLOAD_SHARE_WITH_ME = false;

    // chế độ ANONYMOUS
    public static boolean ANONYMOUS_PERMISSION = true;

    // location my folder on server
    private static String locationYourFolder = null;

    // 1MB
    private static double CONSTANT_1_MB = 1048576;

    private static DefaultTableModel tblMyFileCloudModel;
    private DefaultTableModel tblFolderSharedModel;
    private DefaultTableModel tblFileSharedModel;

    public ClientUI() {
        initComponents();
        
        String serverIP = ClientIPFetcher.getServerIPFromAPI();
        ClientThread.connect("localhost", 42000);
        setLocationRelativeTo(this);
        this.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        pnlLogin.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        pnlRegister.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));

        setColumnTableModel();
        setDefaultEditorTable();
        lblLoading_Register.setVisible(false);
        jLabel15.setVisible(false);
       ClientThread.tranferLayout(pnlContainer, "pnlLogin");
    }

    private static double usedSize = 0;

    public static void loadProcessMemory() {
        usedSize = 0;
        if (!listFileInfo.isEmpty() && listFileInfo != null) {
            listFileInfo.forEach((file) -> {
                if (file.getPrexEmail().trim().equals(folderInfo.getFolderName().trim())) {
                    usedSize += Double.parseDouble(file.getFileSize()) / CONSTANT_1_MB;
                }
            });
//            System.out.println(usedSize);
        }

        double folderSize = Double.parseDouble(folderInfo.getSize().replaceAll(",", "")) / CONSTANT_1_MB;

        jLabel5.setText("Used " + String.format("%,.2f", Math.ceil(usedSize)) + "MB out of "
                + String.format("%,.2f", Math.ceil(folderSize)) + "MB");

        String maximumProcessSize = String.format("%,.0f", Math.ceil(folderSize));
        String valueProcessSize = String.format("%,.0f", Math.ceil(usedSize));

//        System.out.println(maximumProcessSize);
//        System.out.println(valueProcessSize);
        bar_memories.setMaximum(Integer.parseInt(maximumProcessSize.replaceAll(",", "")));
        bar_memories.setValue(Integer.parseInt(valueProcessSize.replaceAll(",", "")));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="set edit table">
    private void setDefaultEditorTable() {
        tblMyFileCloud.setDefaultEditor(Object.class, null);
        tblFolderShared.setDefaultEditor(Object.class, null);
        tblFileShared.setDefaultEditor(Object.class, null);
    }
    // </editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="set up header table model">
    private void setColumnTableModel() {

        tblMyFileCloudModel = (DefaultTableModel) tblMyFileCloud.getModel();
        tblMyFileCloudModel.setColumnIdentifiers(new Object[]{
            "Tên file", "Chủ sở hữu", "Sửa đổi lần cuối", "Định dạng", "Kích cỡ tệp", "Kích cỡ đầy đủ", "FolderID"
        });
        hiddenColumn(tblMyFileCloud, 5);
        hiddenColumn(tblMyFileCloud, 6);

        JTableHeader headerMyFileCloud = tblMyFileCloud.getTableHeader();
        headerMyFileCloud.getColumnModel().getColumn(0).setPreferredWidth(250);
        headerMyFileCloud.getColumnModel().getColumn(1).setPreferredWidth(50);
        headerMyFileCloud.getColumnModel().getColumn(2).setPreferredWidth(150);
        headerMyFileCloud.getColumnModel().getColumn(3).setPreferredWidth(50);

        tblFolderSharedModel = (DefaultTableModel) tblFolderShared.getModel();
        tblFolderSharedModel.setColumnIdentifiers(new Object[]{
            "Folder Id", "Folder Name", "Ngày khởi tạo", "Đường dẫn folder", "Chủ sở hữu", "Quyền"
        });
        hiddenColumnFirst(tblFolderShared);
        hiddenColumn(tblFolderShared, 3);
        hiddenColumn(tblFolderShared, 4);
        hiddenColumn(tblFolderShared, 5);

        tblFileSharedModel = (DefaultTableModel) tblFileShared.getModel();
        tblFileSharedModel.setColumnIdentifiers(new Object[]{
            "File Id", "Tên file", "Chủ sở hữu", "Sửa đổi lần cuối", "Định dạng",
            "Kích cỡ tệp", "Đường dẫn", "Quyền"
        });

        hiddenColumnFirst(tblFileShared);
        hiddenColumn(tblFileShared, 6);
        hiddenColumn(tblFileShared, 7);
    }
    // </editor-fold>  

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="load data model">
    public static void showDataMyFileCloud() {
        // set up table show list file of user
        tblMyFileCloudModel.setRowCount(0);
        
        if (!listFileInfo.isEmpty() && listFileInfo != null ) {
            listFileInfo.forEach((file) -> {
                if (!file.getPrexEmail().trim().equals("anonymous")
                && file.getFolderId().trim().equals(folderSelected.getFolderId().trim())) {
                    tblMyFileCloudModel.addRow(new Object[]{
                        file.getFileName().trim(),
                        file.getPrexEmail().trim(),
                        file.getUploadAt().trim(),
                        file.getFileExtension().trim(),
                        //                        FileExtensions.convertSizeFromSizeString(file.getFileSize(), "MB") + " MB",
                        String.format("%,.5f", Double.parseDouble(file.getFileSize()) / CONSTANT_1_MB) + " MB",
                        file.getFileSize().trim(),
                        file.getFolderId().trim()
                    });
                }
            });
        }

        // set up show list folder child of user
        cmbFolderChild.removeAllItems();
        cmbFolderChild.addItem("--Choose a folder child--");
        if (folderInfo != null && listFolderChildInfo != null && !listFolderChildInfo.isEmpty()) {
            listFolderChildInfo.forEach((folder) -> {
                String folderPath = folder.getFolderPath().substring(folderInfo.getFolderPath().length());
                cmbFolderChild.addItem(folderPath);
            });
        }
    }

    public static void showDataAnonymousFileCloud() {
        // set up table show list file of user
        tblMyFileCloudModel.setRowCount(0);
        if (!listFileInfo.isEmpty() && listFileInfo != null) {
            listFileInfo.forEach((file) -> {
                if (!file.getPrexEmail().trim().equals("anonymous")|| userInfo.getEmail().trim().equals("anonymous")) {
                    tblMyFileCloudModel.addRow(new Object[]{
                        file.getFileName().trim(),
                        file.getPrexEmail().trim(),
                        file.getUploadAt().trim(),
                        file.getFileExtension().trim(),
                        //                        FileExtensions.convertSizeFromSizeString(file.getFileSize(), "MB") + " MB",
                        String.format("%,.5f", Double.parseDouble(file.getFileSize()) / CONSTANT_1_MB) + " MB",
                        file.getFileSize().trim(),
                        file.getFolderId().trim()
                    });
                }
            });
        }

        // set up show list folder child of user
        cmbFolderChild.removeAllItems();
        cmbFolderChild.addItem("--Choose a folder child--");
        if (folderInfo != null) {
            if (!listFolderChildInfo.isEmpty()) {
                listFolderChildInfo.forEach((folder) -> {
                    String folderPath = folder.getFolderPath().substring(folderInfo.getFolderPath().length());
                    cmbFolderChild.addItem(folderPath);
                });
            }
        }
    }

    public static void showDataPublicCloud() {
        // set up table show list file of user
        tblMyFileCloudModel.setRowCount(0);
        if (!listFileInfo.isEmpty() && listFileInfo != null) {
            listFileInfo.forEach((file) -> {
                tblMyFileCloudModel.addRow(new Object[]{
                    file.getFileName().trim(),
                    file.getPrexEmail().trim(),
                    file.getUploadAt().trim(),
                    file.getFileExtension().trim(),
                    String.format("%,.5f", Double.parseDouble(file.getFileSize()) / CONSTANT_1_MB) + " MB",
                    file.getFileSize().trim(),
                    file.getFolderId().trim()
                });
            });
        }
    }

    private void showDataMyFileShare() {
        tblFolderSharedModel.setRowCount(0);
        if (!listFolderShareInfo.isEmpty()) {
            listFolderShareInfo.forEach((folder) -> {
                for (FolderShares folderShares : listFolderSharedInfo) {
                    if (folderShares.getFolderId().trim().equals(folder.getFolderId().trim())) {
                        tblFolderSharedModel.addRow(new Object[]{
                            folder.getFolderId(),
                            folder.getFolderName(),
                            folder.getCreateAt(),
                            folder.getFolderPath(),
                            folder.getEmail(),
                            folderShares.getPermissionId()
                        });
                        break;
                    }
                }

            });
        }

        tblFileSharedModel.setRowCount(0);
        if (!listFileShareInfo.isEmpty()) {
            listFileShareInfo.forEach((file) -> {
                for (FileShares fileShares : listFileSharedInfo) {
                    if (fileShares.getFileId().trim().equals(file.getFileId().trim())) {
                        tblFileSharedModel.addRow(new Object[]{
                            file.getFileId(), file.getFileName(), file.getEmailShare(), file.getUploadAt(), file.getFileExtension(),
                            String.format("%,.5f", Double.parseDouble(file.getFileSize()) / CONSTANT_1_MB) + " MB",
                            file.getSourcePath(),
                            fileShares.getPermissionId()
                        });
                    }
                }
            });
        }
    }
    // </editor-fold>  

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="get values">
    public Folders getFolderInfo() {
        return folderInfo;
    }

    public List<Folders> getListFolderChildInfo() {
        return listFolderChildInfo;
    }

    public Users getUserInfo() {
        return userInfo;
    }

    public List<Files> getListFileInfo() {
        return listFileInfo;
    }

    public List<Permissions> getListPermissionInfo() {
        return listPermissionInfo;
    }

    public List<String> getListNotification() {
        return notifications;
    }
    // </editor-fold>  

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGroup_Sex = new javax.swing.ButtonGroup();
        pnlContainer = new javax.swing.JPanel();
        pnlMain = new javax.swing.JPanel();
        lblUploadNewFile = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        lblSayHelloUser = new javax.swing.JLabel();
        lblSignout = new javax.swing.JLabel();
        lblMinimize = new javax.swing.JLabel();
        lblReload = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        pnlSection = new javax.swing.JPanel();
        pnlMyCloud = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMyFileCloud = new javax.swing.JTable();
        lblTitlePath = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblTitleCmbFolderChild = new javax.swing.JLabel();
        cmbFolderChild = new javax.swing.JComboBox<>();
        btnCreateNewFolder = new javax.swing.JButton();
        btnFolderRoot = new javax.swing.JButton();
        btnDownload = new javax.swing.JButton();
        btnShare = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        pnlShareWithMe = new javax.swing.JPanel();
        lblTitlePath1 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblFolderShared = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblFileShared = new javax.swing.JTable();
        jLabel29 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblMyCloud = new javax.swing.JLabel();
        lblShareWithMe = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        lblPublicCloud = new javax.swing.JLabel();
        bar_memories = new javax.swing.JProgressBar();
        jLabel25 = new javax.swing.JLabel();
        lblTotalNotification = new javax.swing.JLabel();
        background = new javax.swing.JLabel();
        pnlLogin = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtLoginPass = new javax.swing.JPasswordField();
        txtLoginEmail = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        cbRemember = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        lblSignIn = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        btnSignIn_Anonymous = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        pnlRegister = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtRegis_Pass = new javax.swing.JPasswordField();
        txtRegis_FullName = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel20 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel21 = new javax.swing.JLabel();
        cboRegis_FeMale = new javax.swing.JCheckBox();
        cboRegis_Male = new javax.swing.JCheckBox();
        jLabel22 = new javax.swing.JLabel();
        jdtRegis_dbo = new com.toedter.calendar.JDateChooser();
        txtVerifyCode = new javax.swing.JTextField();
        lblVerifyInfo = new javax.swing.JLabel();
        lblRegister = new RoundedRegister(30, new Color(83,187,98));
        lblRegisToLogin = new javax.swing.JLabel();
        txtRegis_Email = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        lblLoading_Register = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(new java.awt.Dimension(1110, 570));

        pnlContainer.setLayout(new java.awt.CardLayout());

        pnlMain.setBackground(new java.awt.Color(255, 255, 255));
        pnlMain.setLayout(null);

        lblUploadNewFile.setFont(new java.awt.Font("Tempus Sans ITC", 1, 14)); // NOI18N
        lblUploadNewFile.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUploadNewFile.setText("Upload New File");
        lblUploadNewFile.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblUploadNewFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblUploadNewFileMouseClicked(evt);
            }
        });
        pnlMain.add(lblUploadNewFile);
        lblUploadNewFile.setBounds(50, 130, 160, 30);

        jLabel2.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(34, 92, 198));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/ftp-2.png"))); // NOI18N
        jLabel2.setText("FTP Client");
        pnlMain.add(jLabel2);
        jLabel2.setBounds(10, 10, 280, 50);
        pnlMain.add(jTextField1);
        jTextField1.setBounds(330, 50, 340, 30);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-shutdown-25.png"))); // NOI18N
        jLabel3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });
        pnlMain.add(jLabel3);
        jLabel3.setBounds(1070, 0, 30, 30);

        lblSayHelloUser.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblSayHelloUser.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSayHelloUser.setText("Xin chào: Phan Thanh Du");
        pnlMain.add(lblSayHelloUser);
        lblSayHelloUser.setBounds(830, 0, 160, 30);

        lblSignout.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSignout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-logout-25.png"))); // NOI18N
        lblSignout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblSignout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSignoutMouseClicked(evt);
            }
        });
        pnlMain.add(lblSignout);
        lblSignout.setBounds(995, 0, 40, 30);

        lblMinimize.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMinimize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-minimize-window-25.png"))); // NOI18N
        lblMinimize.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblMinimize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMinimizeMouseClicked(evt);
            }
        });
        pnlMain.add(lblMinimize);
        lblMinimize.setBounds(1030, 1, 40, 30);

        lblReload.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-reload-35.png"))); // NOI18N
        lblReload.setToolTipText("Refresh");
        lblReload.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblReload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblReloadMouseClicked(evt);
            }
        });
        pnlMain.add(lblReload);
        lblReload.setBounds(808, 50, 40, 30);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-notification-25.png"))); // NOI18N
        jLabel8.setToolTipText("Notifications");
        jLabel8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });
        pnlMain.add(jLabel8);
        jLabel8.setBounds(760, 50, 40, 30);

        pnlSection.setLayout(new java.awt.CardLayout());

        pnlMyCloud.setBackground(new java.awt.Color(255, 255, 255));
        pnlMyCloud.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlMyCloud.setLayout(null);

        tblMyFileCloud.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        tblMyFileCloud.setForeground(new java.awt.Color(0, 0, 204));
        tblMyFileCloud.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblMyFileCloud.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tblMyFileCloud.setGridColor(new java.awt.Color(255, 255, 255));
        tblMyFileCloud.setRowHeight(30);
        tblMyFileCloud.setSelectionBackground(new java.awt.Color(204, 204, 204));
        tblMyFileCloud.setSelectionForeground(new java.awt.Color(0, 0, 153));
        tblMyFileCloud.getTableHeader().setResizingAllowed(false);
        tblMyFileCloud.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tblMyFileCloudMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(tblMyFileCloud);

        pnlMyCloud.add(jScrollPane1);
        jScrollPane1.setBounds(20, 210, 760, 220);

        lblTitlePath.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N
        lblTitlePath.setForeground(new java.awt.Color(34, 92, 198));
        lblTitlePath.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTitlePath.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icons8-drop-down-20.png"))); // NOI18N
        lblTitlePath.setText("My Cloud ");
        lblTitlePath.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        pnlMyCloud.add(lblTitlePath);
        lblTitlePath.setBounds(20, 10, 550, 30);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(51, 51, 51));
        jLabel4.setText("ALL FILES");
        pnlMyCloud.add(jLabel4);
        jLabel4.setBounds(23, 165, 140, 30);

        lblTitleCmbFolderChild.setFont(new java.awt.Font("Script MT Bold", 0, 17)); // NOI18N
        lblTitleCmbFolderChild.setText("Folder Childs:");
        pnlMyCloud.add(lblTitleCmbFolderChild);
        lblTitleCmbFolderChild.setBounds(250, 55, 130, 20);

        cmbFolderChild.setBackground(new java.awt.Color(212, 255, 255));
        cmbFolderChild.setFont(new java.awt.Font("Tempus Sans ITC", 1, 14)); // NOI18N
        cmbFolderChild.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cmbFolderChild.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFolderChildActionPerformed(evt);
            }
        });
        pnlMyCloud.add(cmbFolderChild);
        cmbFolderChild.setBounds(250, 80, 260, 50);

        btnCreateNewFolder.setBackground(new java.awt.Color(212, 255, 255));
        btnCreateNewFolder.setFont(new java.awt.Font("Tempus Sans ITC", 1, 13)); // NOI18N
        btnCreateNewFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-add-23.png"))); // NOI18N
        btnCreateNewFolder.setText("New folder");
        btnCreateNewFolder.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        btnCreateNewFolder.setBorderPainted(false);
        btnCreateNewFolder.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCreateNewFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateNewFolderActionPerformed(evt);
            }
        });
        pnlMyCloud.add(btnCreateNewFolder);
        btnCreateNewFolder.setBounds(640, 10, 130, 33);

        btnFolderRoot.setBackground(new java.awt.Color(212, 255, 255));
        btnFolderRoot.setFont(new java.awt.Font("Tempus Sans ITC", 1, 19)); // NOI18N
        btnFolderRoot.setText("Folder Root");
        btnFolderRoot.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        btnFolderRoot.setBorderPainted(false);
        btnFolderRoot.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFolderRoot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFolderRootActionPerformed(evt);
            }
        });
        pnlMyCloud.add(btnFolderRoot);
        btnFolderRoot.setBounds(20, 80, 180, 50);

        btnDownload.setBackground(new java.awt.Color(212, 255, 255));
        btnDownload.setFont(new java.awt.Font("Tempus Sans ITC", 1, 13)); // NOI18N
        btnDownload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-downloadfile-25.png"))); // NOI18N
        btnDownload.setText(" Download");
        btnDownload.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        btnDownload.setBorderPainted(false);
        btnDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownloadActionPerformed(evt);
            }
        });
        pnlMyCloud.add(btnDownload);
        btnDownload.setBounds(640, 60, 130, 33);

        btnShare.setBackground(new java.awt.Color(212, 255, 255));
        btnShare.setFont(new java.awt.Font("Tempus Sans ITC", 1, 13)); // NOI18N
        btnShare.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-share-23.png"))); // NOI18N
        btnShare.setText(" Share");
        btnShare.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        btnShare.setBorderPainted(false);
        btnShare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShareActionPerformed(evt);
            }
        });
        pnlMyCloud.add(btnShare);
        btnShare.setBounds(640, 110, 130, 33);

        jLabel15.setText("jLabel15");
        pnlMyCloud.add(jLabel15);
        jLabel15.setBounds(390, 190, 45, 16);

        pnlSection.add(pnlMyCloud, "pnlMyCloud");

        pnlShareWithMe.setBackground(new java.awt.Color(255, 255, 255));

        lblTitlePath1.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N
        lblTitlePath1.setForeground(new java.awt.Color(34, 92, 198));
        lblTitlePath1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTitlePath1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icons8-drop-down-20.png"))); // NOI18N
        lblTitlePath1.setText("Share with me ");
        lblTitlePath1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        jButton4.setBackground(new java.awt.Color(212, 255, 255));
        jButton4.setFont(new java.awt.Font("Tempus Sans ITC", 1, 13)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-downloadfile-25.png"))); // NOI18N
        jButton4.setText(" Download");
        jButton4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton4.setBorderPainted(false);
        jButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(51, 51, 51));
        jLabel27.setText("Folders Share:");

        tblFolderShared.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        tblFolderShared.setForeground(new java.awt.Color(0, 0, 204));
        tblFolderShared.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblFolderShared.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tblFolderShared.setGridColor(new java.awt.Color(255, 255, 255));
        tblFolderShared.setRowHeight(30);
        tblFolderShared.setSelectionBackground(new java.awt.Color(204, 204, 204));
        tblFolderShared.setSelectionForeground(new java.awt.Color(0, 0, 153));
        tblFolderShared.getTableHeader().setResizingAllowed(false);
        tblFolderShared.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFolderSharedMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblFolderShared);

        tblFileShared.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        tblFileShared.setForeground(new java.awt.Color(0, 0, 204));
        tblFileShared.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblFileShared.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tblFileShared.setGridColor(new java.awt.Color(255, 255, 255));
        tblFileShared.setRowHeight(30);
        tblFileShared.setSelectionBackground(new java.awt.Color(204, 204, 204));
        tblFileShared.setSelectionForeground(new java.awt.Color(0, 0, 153));
        tblFileShared.getTableHeader().setResizingAllowed(false);
        tblFileShared.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tblFileSharedMouseReleased(evt);
            }
        });
        jScrollPane3.setViewportView(tblFileShared);

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(51, 51, 51));
        jLabel29.setText("Files Share:");

        javax.swing.GroupLayout pnlShareWithMeLayout = new javax.swing.GroupLayout(pnlShareWithMe);
        pnlShareWithMe.setLayout(pnlShareWithMeLayout);
        pnlShareWithMeLayout.setHorizontalGroup(
            pnlShareWithMeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlShareWithMeLayout.createSequentialGroup()
                .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlShareWithMeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlShareWithMeLayout.createSequentialGroup()
                        .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(pnlShareWithMeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitlePath1, javax.swing.GroupLayout.PREFERRED_SIZE, 547, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(233, Short.MAX_VALUE))
            .addGroup(pnlShareWithMeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlShareWithMeLayout.createSequentialGroup()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        pnlShareWithMeLayout.setVerticalGroup(
            pnlShareWithMeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlShareWithMeLayout.createSequentialGroup()
                .addGroup(pnlShareWithMeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlShareWithMeLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(lblTitlePath1)
                        .addGap(75, 75, 75)
                        .addGroup(pnlShareWithMeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlShareWithMeLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)))
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlShareWithMeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlShareWithMeLayout.createSequentialGroup()
                    .addGap(0, 174, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 19, Short.MAX_VALUE)))
        );

        pnlSection.add(pnlShareWithMe, "pnlShareWithMe");

        pnlMain.add(pnlSection);
        pnlSection.setBounds(310, 110, 790, 440);

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 254));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Used 0MB out of 1GB");
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlMain.add(jLabel5);
        jLabel5.setBounds(40, 480, 220, 30);

        lblMyCloud.setFont(new java.awt.Font("Segoe UI Semibold", 3, 18)); // NOI18N
        lblMyCloud.setForeground(new java.awt.Color(255, 255, 254));
        lblMyCloud.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-network-drive-25.png"))); // NOI18N
        lblMyCloud.setText("   My Cloud");
        lblMyCloud.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblMyCloud.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMyCloudMouseClicked(evt);
            }
        });
        pnlMain.add(lblMyCloud);
        lblMyCloud.setBounds(40, 190, 180, 40);

        lblShareWithMe.setFont(new java.awt.Font("Segoe UI Semibold", 3, 18)); // NOI18N
        lblShareWithMe.setForeground(new java.awt.Color(255, 255, 254));
        lblShareWithMe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icons-share-25.png"))); // NOI18N
        lblShareWithMe.setText("   Share with me");
        lblShareWithMe.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblShareWithMe.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblShareWithMeMouseClicked(evt);
            }
        });
        pnlMain.add(lblShareWithMe);
        lblShareWithMe.setBounds(40, 250, 180, 40);

        jSeparator6.setBackground(new java.awt.Color(204, 204, 204));
        jSeparator6.setForeground(new java.awt.Color(204, 204, 204));
        pnlMain.add(jSeparator6);
        jSeparator6.setBounds(10, 393, 270, 10);

        lblPublicCloud.setFont(new java.awt.Font("Segoe UI Semibold", 3, 18)); // NOI18N
        lblPublicCloud.setForeground(new java.awt.Color(255, 255, 254));
        lblPublicCloud.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-public-cloud-25.png"))); // NOI18N
        lblPublicCloud.setText("   Public Cloud");
        lblPublicCloud.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblPublicCloud.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblPublicCloudMouseClicked(evt);
            }
        });
        pnlMain.add(lblPublicCloud);
        lblPublicCloud.setBounds(40, 310, 180, 40);

        bar_memories.setBackground(new java.awt.Color(51, 51, 51));
        bar_memories.setForeground(new java.awt.Color(153, 153, 255));
        bar_memories.setMaximum(0);
        bar_memories.setOpaque(true);
        pnlMain.add(bar_memories);
        bar_memories.setBounds(30, 400, 250, 10);

        jLabel25.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 254));
        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-cloud-25.png"))); // NOI18N
        jLabel25.setText("  Memories");
        jLabel25.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlMain.add(jLabel25);
        jLabel25.setBounds(60, 430, 170, 30);

        lblTotalNotification.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblTotalNotification.setForeground(new java.awt.Color(255, 0, 0));
        lblTotalNotification.setText("0");
        pnlMain.add(lblTotalNotification);
        lblTotalNotification.setBounds(787, 45, 10, 14);

        background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/background.png"))); // NOI18N
        background.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 51)));
        pnlMain.add(background);
        background.setBounds(0, 0, 1112, 572);

        pnlContainer.add(pnlMain, "pnlMain");

        pnlLogin.setBackground(new java.awt.Color(34, 92, 198));
        pnlLogin.setLayout(null);

        jPanel1.setBackground(new java.awt.Color(0, 153, 255));
        jPanel1.setLayout(null);

        jLabel10.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Password");
        jPanel1.add(jLabel10);
        jLabel10.setBounds(40, 240, 100, 30);

        jLabel11.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Email");
        jPanel1.add(jLabel11);
        jLabel11.setBounds(30, 160, 70, 30);

        txtLoginPass.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        txtLoginPass.setBorder(null);
        txtLoginPass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtLoginPassKeyReleased(evt);
            }
        });
        jPanel1.add(txtLoginPass);
        txtLoginPass.setBounds(40, 280, 320, 30);

        txtLoginEmail.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        txtLoginEmail.setBorder(null);
        txtLoginEmail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtLoginEmailKeyReleased(evt);
            }
        });
        jPanel1.add(txtLoginEmail);
        txtLoginEmail.setBounds(40, 200, 320, 30);
        jPanel1.add(jSeparator1);
        jSeparator1.setBounds(40, 270, 320, 10);
        jPanel1.add(jSeparator2);
        jSeparator2.setBounds(40, 190, 320, 10);

        cbRemember.setBackground(new java.awt.Color(0, 153, 255));
        cbRemember.setBorder(null);
        cbRemember.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.add(cbRemember);
        cbRemember.setBounds(40, 320, 20, 20);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Remember password");
        jPanel1.add(jLabel12);
        jLabel12.setBounds(60, 320, 180, 20);

        lblSignIn.setBackground(new java.awt.Color(255, 255, 255));
        lblSignIn.setFont(new java.awt.Font("Rockwell Condensed", 0, 18)); // NOI18N
        lblSignIn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSignIn.setText("Sign In");
        lblSignIn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblSignIn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSignInMouseClicked(evt);
            }
        });
        jPanel1.add(lblSignIn);
        lblSignIn.setBounds(130, 370, 120, 40);
        lblSignIn.setOpaque(true);  // Bắt buộc phải có dòng này
        lblSignIn.setBackground(new Color(255, 255, 255)); // Ví dụ: màu cam nhạt

        jLabel13.setFont(new java.awt.Font("Rockwell Condensed", 1, 36)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Login Client");
        jPanel1.add(jLabel13);
        jLabel13.setBounds(80, 60, 230, 70);

        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("You not have an account??? Register>>>");
        jLabel26.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel26.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel26MouseClicked(evt);
            }
        });
        jPanel1.add(jLabel26);
        jLabel26.setBounds(90, 500, 230, 20);

        btnSignIn_Anonymous.setBackground(new java.awt.Color(0, 0, 0));
        btnSignIn_Anonymous.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        btnSignIn_Anonymous.setForeground(new java.awt.Color(255, 255, 255));
        btnSignIn_Anonymous.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/icon-anonymous-19.png"))); // NOI18N
        btnSignIn_Anonymous.setText(" Sign in with anonymous permisson");
        btnSignIn_Anonymous.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        btnSignIn_Anonymous.setBorderPainted(false);
        btnSignIn_Anonymous.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSignIn_Anonymous.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSignIn_AnonymousActionPerformed(evt);
            }
        });
        jPanel1.add(btnSignIn_Anonymous);
        btnSignIn_Anonymous.setBounds(80, 460, 240, 30);
        btnSignIn_Anonymous.getAccessibleContext().setAccessibleName("Sign in with anonymous permisson");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("X");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });
        jPanel1.add(jLabel1);
        jLabel1.setBounds(350, 10, 30, 30);

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("_");
        jLabel23.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jLabel23.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel23.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel23MouseClicked(evt);
            }
        });
        jPanel1.add(jLabel23);
        jLabel23.setBounds(310, 10, 30, 30);

        pnlLogin.add(jPanel1);
        jPanel1.setBounds(360, 10, 390, 530);

        pnlContainer.add(pnlLogin, "pnlLogin");

        pnlRegister.setBackground(new java.awt.Color(255, 255, 255));
        pnlRegister.setLayout(null);

        jPanel2.setBackground(new java.awt.Color(0, 153, 255));
        jPanel2.setLayout(null);

        jLabel17.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Password:");
        jPanel2.add(jLabel17);
        jLabel17.setBounds(30, 180, 100, 20);

        jLabel18.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Email:");
        jPanel2.add(jLabel18);
        jLabel18.setBounds(30, 110, 60, 20);

        txtRegis_Pass.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        txtRegis_Pass.setBorder(null);
        txtRegis_Pass.setMargin(new java.awt.Insets(10, 6, 2, 10));
        txtRegis_Pass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtRegis_PassKeyReleased(evt);
            }
        });
        jPanel2.add(txtRegis_Pass);
        txtRegis_Pass.setBounds(30, 210, 320, 30);

        txtRegis_FullName.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        txtRegis_FullName.setBorder(null);
        txtRegis_FullName.setMargin(new java.awt.Insets(10, 6, 2, 10));
        txtRegis_FullName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtRegis_FullNameKeyReleased(evt);
            }
        });
        jPanel2.add(txtRegis_FullName);
        txtRegis_FullName.setBounds(30, 280, 320, 30);
        jPanel2.add(jSeparator3);
        jSeparator3.setBounds(30, 200, 320, 10);
        jPanel2.add(jSeparator4);
        jSeparator4.setBounds(30, 130, 320, 10);

        jLabel20.setFont(new java.awt.Font("Arial", 1, 26)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Register Cloud");
        jPanel2.add(jLabel20);
        jLabel20.setBounds(70, 40, 250, 40);

        jLabel19.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Fullname:");
        jPanel2.add(jLabel19);
        jLabel19.setBounds(30, 250, 100, 20);
        jPanel2.add(jSeparator5);
        jSeparator5.setBounds(30, 270, 320, 10);

        jLabel21.setFont(new java.awt.Font("Rockwell Condensed", 0, 22)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("DoB:");
        jPanel2.add(jLabel21);
        jLabel21.setBounds(10, 400, 70, 30);

        cboRegis_FeMale.setBackground(new java.awt.Color(0, 153, 255));
        btnGroup_Sex.add(cboRegis_FeMale);
        cboRegis_FeMale.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        cboRegis_FeMale.setForeground(new java.awt.Color(255, 255, 255));
        cboRegis_FeMale.setText("Female");
        jPanel2.add(cboRegis_FeMale);
        cboRegis_FeMale.setBounds(180, 360, 81, 20);

        cboRegis_Male.setBackground(new java.awt.Color(0, 153, 255));
        btnGroup_Sex.add(cboRegis_Male);
        cboRegis_Male.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        cboRegis_Male.setForeground(new java.awt.Color(255, 255, 255));
        cboRegis_Male.setSelected(true);
        cboRegis_Male.setText("Male");
        jPanel2.add(cboRegis_Male);
        cboRegis_Male.setBounds(90, 360, 70, 20);

        jLabel22.setFont(new java.awt.Font("Rockwell Condensed", 0, 22)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Sex:");
        jPanel2.add(jLabel22);
        jLabel22.setBounds(10, 360, 70, 20);

        jdtRegis_dbo.setBackground(new java.awt.Color(0, 153, 255));
        jPanel2.add(jdtRegis_dbo);
        jdtRegis_dbo.setBounds(90, 400, 240, 30);

        txtVerifyCode.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtVerifyCode.setMargin(new java.awt.Insets(10, 2, 2, 10));
        jPanel2.add(txtVerifyCode);
        txtVerifyCode.setBounds(100, 460, 190, 40);

        lblVerifyInfo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblVerifyInfo.setForeground(new java.awt.Color(255, 255, 255));
        lblVerifyInfo.setText("Verify Code");
        jPanel2.add(lblVerifyInfo);
        lblVerifyInfo.setBounds(160, 440, 80, 20);

        lblRegister.setFont(new java.awt.Font("Rockwell Condensed", 0, 22)); // NOI18N
        lblRegister.setForeground(new java.awt.Color(255, 255, 255));
        lblRegister.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRegister.setText("Register");
        lblRegister.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblRegister.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRegisterMouseClicked(evt);
            }
        });
        jPanel2.add(lblRegister);
        lblRegister.setBounds(130, 508, 130, 30);

        lblRegisToLogin.setForeground(new java.awt.Color(204, 204, 204));
        lblRegisToLogin.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRegisToLogin.setText("You have an account??? Login>>>");
        lblRegisToLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblRegisToLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRegisToLoginMouseClicked(evt);
            }
        });
        jPanel2.add(lblRegisToLogin);
        lblRegisToLogin.setBounds(100, 540, 200, 16);

        txtRegis_Email.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        txtRegis_Email.setBorder(null);
        txtRegis_Email.setMargin(new java.awt.Insets(10, 6, 2, 10));
        jPanel2.add(txtRegis_Email);
        txtRegis_Email.setBounds(30, 140, 320, 30);

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setText("X");
        jLabel30.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jLabel30.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel30.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel30MouseClicked(evt);
            }
        });
        jPanel2.add(jLabel30);
        jLabel30.setBounds(350, 10, 30, 30);

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setText("_");
        jLabel24.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jLabel24.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel24.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel24MouseClicked(evt);
            }
        });
        jPanel2.add(jLabel24);
        jLabel24.setBounds(310, 10, 30, 30);

        lblLoading_Register.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLoading_Register.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/loading-bar.gif"))); // NOI18N
        lblLoading_Register.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel2.add(lblLoading_Register);
        lblLoading_Register.setBounds(90, 320, 200, 24);

        pnlRegister.add(jPanel2);
        jPanel2.setBounds(380, 0, 390, 570);

        pnlContainer.add(pnlRegister, "pnlRegister");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 1110, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtLoginPassKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtLoginPassKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            handleSignIn();
        }
    }//GEN-LAST:event_txtLoginPassKeyReleased

    private void txtLoginEmailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtLoginEmailKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            handleSignIn();
        }
    }//GEN-LAST:event_txtLoginEmailKeyReleased

    private void lblSignInMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSignInMouseClicked
        handleSignIn();
    }//GEN-LAST:event_lblSignInMouseClicked

    private void txtRegis_PassKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRegis_PassKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRegis_PassKeyReleased

    private void txtRegis_FullNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRegis_FullNameKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRegis_FullNameKeyReleased

    private void jLabel26MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel26MouseClicked
        repaint();
        ClientThread.tranferLayout(pnlContainer, "pnlRegister");
        lblVerifyInfo.setVisible(false);
        txtVerifyCode.setVisible(false);
    }//GEN-LAST:event_jLabel26MouseClicked

    private void lblRegisToLoginMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRegisToLoginMouseClicked
        repaint();
        ClientThread.tranferLayout(pnlContainer, "pnlLogin");
        txtRegis_Email.setText("");
        txtRegis_Pass.setText("");
        txtRegis_FullName.setText("");
        isVerify = false;
        verifyCode = 0;
        lblVerifyInfo.setVisible(false);
        txtVerifyCode.setVisible(false);

        txtRegis_Email.setEnabled(true);
        txtRegis_Pass.setEnabled(true);
        txtRegis_FullName.setEnabled(true);
    }//GEN-LAST:event_lblRegisToLoginMouseClicked

    public static void resetSignOut() {

        pnlLogin.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        ClientThread.tranferLayout(pnlContainer, "pnlLogin");
        userInfo = null;
        folderInfo = null;
        folderSelected = null;
        listFolderChildInfo = null;
        listFileInfo = null;
        listFileShareInfo = null;
        listFolderChildInfo = null;
        listFolderShareInfo = null;
        listPermissionInfo = null;
        locationYourFolder = null;
        notifications.removeAll(notifications);
        TOTAL_NOTIFICATIONS = 0;
        loadCountNewNotification(0);

        lblMyCloud.setVisible(true);
        lblShareWithMe.setVisible(true);
        lblTitleCmbFolderChild.setVisible(true);
        btnFolderRoot.setVisible(true);
        cmbFolderChild.setVisible(true);
        btnCreateNewFolder.setVisible(true);
        btnShare.setVisible(true);

        lblTitlePath.setText("My Cloud ");
        setDefaultProcessHandler();
        setVerifyCode(0);
    }

    private void lblSignoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSignoutMouseClicked

        int value = JOptionPane.showConfirmDialog(this, "You will logout then click OK.!\nAre you sure.?", "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (value == JOptionPane.YES_OPTION) {
            if (!userInfo.getFullName().trim().equals("anonymous")) {
                ClientThread.request("signout", userInfo.getEmail());
            }
            repaint();
            jLabel25.setVisible(true);
            bar_memories.setVisible(true);
            jLabel5.setVisible(true);
            resetSignOut();
        }
    }//GEN-LAST:event_lblSignoutMouseClicked

    private void lblRegisterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRegisterMouseClicked
        // set user
        Users user = new Users();
        user.setEmail(txtRegis_Email.getText());
        user.setPassword(String.valueOf(txtRegis_Pass.getPassword()));
        user.setFullName(txtRegis_FullName.getText());

        // check đã kiểm tra đầu vào
        if (!isVerify) {  // nếu chưa check

            ClientThread.request("verify_register", user);
            while (processHandler) {
                System.out.println("watting handler register...");
                // do not something...
                // sẽ dừng khi hoàn tất tiến trình kiểm tra đầu vào
            }
            //  hiển thị thông báo
            Message(messageResult);
            if (statusResult) {     // nếu trạng thái kiểm tra đầu vào ok
                // => hiển thị input text verify code
                lblVerifyInfo.setVisible(true);
                txtVerifyCode.setVisible(true);
                isVerify = true;    // set giá trị thành đã check

                txtRegis_Email.setEnabled(false);
                txtRegis_Pass.setEnabled(false);
                txtRegis_FullName.setEnabled(false);
            }
        } else {
            // kiểm tra giá trị của verifyCode
            if (txtVerifyCode.getText().trim().equals(String.valueOf(verifyCode).trim())) {

                // kiểm tra thời gian của verifyCode còn hạn ko
                if (DateHelper.compare(timestamp)) {
                    if (cboRegis_Male.isSelected()) {
                        user.setSex("Male");
                    } else {
                        user.setSex("FeMale");
                    }
                    if (jdtRegis_dbo.getDate() == null) {
                        user.setDob(DateHelper.Now());
                    } else {
                        user.setDob(DateHelper.formatDate(jdtRegis_dbo.getDate()));
                    }
                    user.setStatus("unlock");
                    user.setCreateAt(DateHelper.Now());
                    ClientThread.request("register", user);
                    while (processHandler) {
                        System.out.println("watting handler register...");
                        // do not something...
                    }
                    Message(messageResult);
                    if (statusResult) {     // result ok
                        repaint();
                        ClientThread.tranferLayout(pnlContainer, "pnlLogin");
                        txtRegis_Email.setText("");
                        txtRegis_Pass.setText("");
                        txtRegis_FullName.setText("");
                        txtVerifyCode.setText("");
                        isVerify = false;
                        verifyCode = 0;
                        lblVerifyInfo.setVisible(false);
                        txtVerifyCode.setVisible(false);

                        txtRegis_Email.setEnabled(true);
                        txtRegis_Pass.setEnabled(true);
                        txtRegis_FullName.setEnabled(true);
                    }
                } else {
                    Message("Mã xác thực đã hết hạn. Vui lòng lấy mã lại.!!!");
                    isVerify = false;
                    verifyCode = 0;
                    lblVerifyInfo.setVisible(false);
                    txtVerifyCode.setVisible(false);
                    txtVerifyCode.setText("");

                    txtRegis_Email.setEnabled(true);
                    txtRegis_Pass.setEnabled(true);
                    txtRegis_FullName.setEnabled(true);
                }
            } else {
                Message("Mã xác thực không chính xác.!!!");
            }
        }
        setDefaultProcessHandler();
    }//GEN-LAST:event_lblRegisterMouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        shutdown();
    }//GEN-LAST:event_jLabel3MouseClicked

    private void lblUploadNewFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblUploadNewFileMouseClicked

        // upload to my cloud
        if (!IS_UPLOAD_SHARE_WITH_ME) {
            handleUploadFile();
        } else {
           handleUploadFileToFolderShare();
        }
    }//GEN-LAST:event_lblUploadNewFileMouseClicked

    private void btnCreateNewFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateNewFolderActionPerformed
        CreateNewFolder dialog = new CreateNewFolder(this, rootPaneCheckingEnabled);
        dialog.show();
    }//GEN-LAST:event_btnCreateNewFolderActionPerformed

    private void cmbFolderChildActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFolderChildActionPerformed
        String compare = "--Choose a folder child--";
        try {
            if (cmbFolderChild.getSelectedItem().toString() != null) {
                String selectedFolderChildName = cmbFolderChild.getSelectedItem().toString();
                if (!selectedFolderChildName.toLowerCase()
                        .equalsIgnoreCase(compare.toLowerCase())) {
                    lblTitlePath.setText("My Cloud > " + selectedFolderChildName + " ");

                    // duyệt danh sách folder con cháu chít để gán value cho folderSelected
                    listFolderChildInfo.forEach((folderChild) -> {
                        String folderPath = folderChild.getFolderPath().trim().substring(folderInfo.getFolderPath().length());
                        if (folderPath.equals(selectedFolderChildName.trim())) {
                            folderSelected = folderChild;
                        }
                    });

                    // lấy ra đường dẫn đang đc chọn từ combobox
                    locationYourFolder = folderSelected.getFolderPath();

                    // load file in folder children
                    tblMyFileCloudModel.setRowCount(0);
                    try {
                        if (!listFileInfo.isEmpty()) {
                            listFileInfo.forEach((file) -> {
                                if (file.getSourcePath().trim().equals(locationYourFolder.trim())) {
                                    tblMyFileCloudModel.addRow(new Object[]{
                                        file.getFileName().trim(),
                                        file.getPrexEmail().trim(),
                                        file.getUploadAt().trim(),
                                        file.getFileExtension().trim(),
                                        FileExtensions.convertSizeFromSizeString(file.getFileSize(), "MB") + " MB",
                                        file.getFileSize().trim(),
                                        file.getFolderId().trim()
                                    });
                                }
                            });
                        }
                    } catch (Exception ex) {
//                        System.err.println("Xảy ra lỗi khi load data của user lên UI vì listFileInfo == null " + ex);
                    }
                    return;
                } else {
                    lblTitlePath.setText("My Cloud ");
                    locationYourFolder = folderInfo.getFolderPath();
                    folderSelected = folderInfo; // ✅ Cập nhật lại folder gốc
                    return;
                }
            }
        } catch (Exception ex) {
//            System.err.println("combobox folder child lấy lần đầu bị null" + ex);
            lblTitlePath.setText("My Cloud ");
            locationYourFolder = folderInfo.getFolderPath();
        }
        prexEmailInfo = userInfo.getEmail().split("@")[0];
    }//GEN-LAST:event_cmbFolderChildActionPerformed

    private void btnFolderRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFolderRootActionPerformed
        lblTitlePath.setText("My Cloud ");
        locationYourFolder = folderInfo.getFolderPath();
        folderSelected = folderInfo;
        prexEmailInfo = userInfo.getEmail().split("@")[0];

        try {
            showDataMyFileCloud();
        } catch (Exception ex) {
//            System.err.println("Xảy ra lỗi khi load data của user lên UI vì có danh sách bị null - " + ex);
        }
    }//GEN-LAST:event_btnFolderRootActionPerformed

    private void lblMyCloudMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMyCloudMouseClicked
        prexEmailInfo = userInfo.getEmail().split("@")[0];
        try {
            loadProcessMemory();
            showDataMyFileCloud();
            lblTitleCmbFolderChild.setVisible(true);
            btnFolderRoot.setVisible(true);
            cmbFolderChild.setVisible(true);
            btnCreateNewFolder.setVisible(true);
            btnShare.setVisible(true);
            IS_UPLOAD_SHARE_WITH_ME = false;
        } catch (Exception ex) {
//            System.err.println("Xảy ra lỗi khi load data của user lên UI vì có danh sách bị null - " + ex);
        }

        // thiết lập đường dẫn root thư mục của client trên server
        // phục vụ cho upload/download file
        locationYourFolder = folderInfo.getFolderPath();
        folderSelected = folderInfo;

        // redirect view
        repaint();
        ClientThread.tranferLayout(pnlContainer, "pnlMain");
        ClientThread.tranferLayout(pnlSection, "pnlMyCloud");
    }//GEN-LAST:event_lblMyCloudMouseClicked

    private void lblPublicCloudMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblPublicCloudMouseClicked

        IS_UPLOAD_SHARE_WITH_ME = false;

        // check user có quyền truy cập anonymous ko???
        if (userInfo.getAnonymousPermission().trim().equals("unlock")) {
            // thiết lập đường dẫn thư mục chung anonymous
            locationYourFolder = folderInfo.getFolderPath();

            List<String> strToken = new ArrayList<>();
            String strSub = "";
            StringTokenizer st = new StringTokenizer(locationYourFolder);
            while (st.hasMoreTokens()) {
                String value = st.nextToken("/");
                strToken.add(value);
            }
            for (int i = 0; i < strToken.size(); i++) {
                if (i != (strToken.size() - 1)) {
                    strSub += "/" + strToken.get(i);
                }
            }
            locationYourFolder = strSub + "/anonymous";
            folderSelected = new Folders("anonymous", "unlock");
            prexEmailInfo = "anonymous";
//            System.out.println(locationYourFolder);

            // load file public in anonymous folder
            lblTitleCmbFolderChild.setVisible(false);
            btnFolderRoot.setVisible(false);
            cmbFolderChild.setVisible(false);
            btnCreateNewFolder.setVisible(false);
            btnShare.setVisible(false);
            lblTitlePath.setText("Public Cloud ");
            tblMyFileCloudModel.setRowCount(0);

            try {
                if (!listFileInfo.isEmpty()) {
                    listFileInfo.forEach((file) -> {
                        if (file.getSourcePath().trim().equals(locationYourFolder.trim())) {
                            tblMyFileCloudModel.addRow(new Object[]{
                                file.getFileName().trim(),
                                file.getPrexEmail().trim(),
                                file.getUploadAt().trim(),
                                file.getFileExtension().trim(),
                                FileExtensions.convertSizeFromSizeString(file.getFileSize(), "MB") + " MB",
                                file.getFileSize().trim(),
                                file.getFolderId().trim()
                            });
                        }
                    });
                }
            } catch (Exception ex) {
//                System.err.println("Xảy ra lỗi khi load data của user lên UI vì listFileInfo bị null -" + ex);
            }
            ClientThread.tranferLayout(pnlSection, "pnlMyCloud");
        } else {
            Message("Bạn không có quyền truy cập chế độ anonymous.!!!");
        }

    }//GEN-LAST:event_lblPublicCloudMouseClicked

    private Folders getFolderById(String folderId) {
        if (folderInfo.getFolderId().equals(folderId)) return folderInfo;
        for (Folders f : listFolderChildInfo) {
            if (f.getFolderId().equals(folderId)) return f;
        }
        return null;
    }

    private void btnDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownloadActionPerformed

        // KHI ĐĂNG NHẬP VỚI QUYỀN ANONYMOUS
        // KHI ĐĂNG NHẬP VỚI QUYỀN USER
        // check user có đc quyền download ko???
        if (userInfo.getPermissionId().trim().toLowerCase().equals("all")
                || userInfo.getPermissionId().trim().toLowerCase().equals("d")) {

            // lấy ra số dòng đc chọn
            int selectedRow = tblMyFileCloud.getSelectedRow();

            // check danh sách file có rỗng ko???
            if (listFileInfo.isEmpty()) {
                Message("Không có file nào trên cloud của bạn.!!!");
            } else {

                // kiểm tra đã chọn file cần download chưa???
                if (selectedRow == -1) {
                    Message("Vui lòng chọn một file cần download.!!!");
                } else {

                    /* Kiểm tra user có quyền download file từ folder này ko???
             * + B1: lấy ra folderId của file đc chọn
             * + B2: kiểm tra folderId vs folderInfo.getFolderId() có trùng nhau k
             * TH1: trùng thì check quyền FolderUserPermission -> để quyết định có đc download hay ko -> Stop
             * TH2: ko trùng thì đi tới bước 3
             * + B3: check folderId có thuộc folder anonymous ko ??? -> có -> gán flag = true -> stop check ? ko -> B4
             * + B4: duyệt danh sách các folder con của user
             * + B5: thực hiện tương tự B2 -> check đến khi nào tìm thấy folderId trùng nhau thì break rồi nhảy vào 2 TH
                     */
                     boolean flagCheckAllowUser = false;
                     String folderIdSelected = tblMyFileCloud.getValueAt(selectedRow, 6).toString(); // B1
                     if (folderIdSelected.trim().equals(folderInfo.getFolderId().trim())) {    // B2
                         if (folderInfo.getFolderUserPermission().trim().equals("unlock")) {   // B2 -> TH1
                             flagCheckAllowUser = true;
                         }
                     } else {  // B2 -> TH2
                         if (folderIdSelected.trim().equals("anonymous")) {  // B3
                             flagCheckAllowUser = true;
                         } else {
                             for (Folders folder : listFolderChildInfo) {    // B4
                                 if (folderIdSelected.trim().equals(folder.getFolderId().trim())) {    // B5 quay lại B2 check
                                     if (folder.getFolderUserPermission().trim().equals("unlock")) {
                                         flagCheckAllowUser = true;
                                     }
                                     break;  // break vòng lặp ngay khi tìm thấy folderId trùng
                                 }
                             }
                         }
                     }

                    /* kiểm tra cờ:
             * flagCheckAllowUser = true -> cho phép
             * flagCheckAllowUser = false -> ko cho phép
                     */
                    if (flagCheckAllowUser) {
                        // lấy ra kích thước file
                        String fileSize = tblMyFileCloud.getValueAt(selectedRow, 5).toString();

                        // kiểm tra kích thước file download có vượt mức cho phép ko???
                        if (Double.parseDouble(fileSize) <= Double.parseDouble(userInfo.getFileSizeDownload().trim())) {

                            // chọn đường dẫn download file
                            JFileChooser destFile = new JFileChooser();
                            destFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                            destFile.setCurrentDirectory(new File(System.getProperty("user.home")));
                            destFile.showOpenDialog(this);
                            File selectedFile = destFile.getSelectedFile();
                            if (selectedFile != null) {

                                String destPathDownload = selectedFile.getAbsolutePath();

                                // kiểm tra đã chọn đường dẫn download chưa???
                                if (!destPathDownload.trim().equals("")) {
                                    boolean checkSourcePath = false;
                                    String fileName = tblMyFileCloud.getValueAt(selectedRow, 0).toString();
//                            String desDownloadPath = FileExtensions.replaceBackslashes(System.getProperty("user.home"))
//                                    + "/Downloads/";
                                    FileDownloadInfo fileDownloadInfo = new FileDownloadInfo();
                                    fileDownloadInfo.setFileName(fileName);
                                    fileDownloadInfo.setDestinationPath(FileExtensions.replaceBackslashes(destPathDownload) + "/");
                                    fileDownloadInfo.setFolderId(folderIdSelected);
                      

                                    // kiểm tra có tồn tại file ko, dựa vào tên file đc chọn
                                    for (Files file : listFileInfo) {
                                        if (file.getFileName().trim().equals(fileName.trim())
                                            && file.getFolderId().trim().equals(folderIdSelected.trim())) {
                                            fileDownloadInfo.setSourceFilePath(file.getSourcePath());
                                            checkSourcePath = true;
                                            break;
                                        }
                                    }
                                    if (checkSourcePath) {
                                        ClientThread.request("download_file", fileDownloadInfo);
                                        try {
                                            Thread thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new LoadingDownloadFile();
                                                }
                                            });
                                            thread.start();
                                        } catch (Exception e) {
                                        }
//                                        Message("Tải xuống thành công.!!!");
                                        return;
                                    } else {
                                        Message("File không tồn tại hoặc đã bị xóa.!!!");
                                    }
                                } else {
                                    Message("Vui lòng chọn đường dẫn download file.!!!");
                                }
                            } else {
                                Message("Vui lòng chọn đường dẫn download file.!!!");
                            }

                        } else {
                            Message("Kích thước file download tối đa"
                                    + String.format("%,.2f", (Double.parseDouble(userInfo.getFileSizeDownload().trim().replaceAll(",", "")) / 1024))
                                    + "KB.!!!");
                        }
                    } else {
                        Message("Bạn không thể download file từ thư mục này.\nVì bạn đã bị lock quyền user trong folder này.\nVui lòng quay lại sau.!!!");
                    }
                }
            }
        } else {
            Message("Chức năng download của bạn đã bị chặn.!!!");
        }
    }//GEN-LAST:event_btnDownloadActionPerformed

    private void btnSignIn_AnonymousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSignIn_AnonymousActionPerformed

        if (ANONYMOUS_PERMISSION) {
            ClientThread.request("authenticate_anonymous_permission", "anonymous");

            while (processHandler) {
                System.out.println("watting handler authenticate...");
            }

            Message(messageResult);
            if (statusResult) {
                lblSayHelloUser.setText("Xin chào: " + userInfo.getFullName());
                txtLoginEmail.setText("");
                txtLoginPass.setText("");
                try {
                    showDataPublicCloud();
                } catch (Exception ex) {
//                    System.err.println("Đã bỏ qua func showDataMyFileCloud - " + ex);
                }

                locationYourFolder = folderInfo.getFolderPath();
                folderSelected = folderInfo;
                prexEmailInfo = "anonymous";

                lblMyCloud.setVisible(false);
                lblShareWithMe.setVisible(false);
                lblTitleCmbFolderChild.setVisible(false);
                btnFolderRoot.setVisible(false);
                cmbFolderChild.setVisible(false);
                btnCreateNewFolder.setVisible(false);
                btnShare.setVisible(false);
                jLabel25.setVisible(false);
                bar_memories.setVisible(false);
                jLabel5.setVisible(false);
                lblTitlePath.setText("Public Cloud ");
                // redirect view
                repaint();
                ClientThread.tranferLayout(pnlContainer, "pnlMain");
                ClientThread.tranferLayout(pnlSection, "pnlMyCloud");
            }
            setDefaultProcessHandler();
        } else {
            Message("Bạn không có quyền truy cập chế độ ẩn danh.!!!");
        }
    }//GEN-LAST:event_btnSignIn_AnonymousActionPerformed

    public static List<Users> listUserSearch = new ArrayList<>();
    public static boolean processFetchUsers = true;
    private void btnShareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShareActionPerformed
        ClientThread.request("FETCH_USERS", userInfo.getEmail().trim());  
        while (processFetchUsers) {
            System.out.println("fetching users...");
        }
        SharePeople share = new SharePeople(this, rootPaneCheckingEnabled);
        share.show();
    }//GEN-LAST:event_btnShareActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        // check user có đc quyền download ko???
        if (userInfo.getPermissionId().trim().toLowerCase().equals("all")
                || userInfo.getPermissionId().trim().toLowerCase().equals("d")) {

            // check danh sách file đc chia sẻ có ko???
            if (listFileShareInfo.isEmpty()) {
                Message("Bạn chưa đc chia sẻ file nào.!!!");
            } else {
                int selectedRow = tblFileShared.getSelectedRow();

                // check đã chọn file cần download chưa
                if (selectedRow == -1) {
                    Message("Vui lòng chọn 1 file cần download.!!!");
                } else {
                    String permission = tblFileShared.getValueAt(selectedRow, 7).toString();
                    if (permission.trim().equals("d") || permission.trim().equals("a")) {

                        // chọn đường dẫn download file
                        JFileChooser destFile = new JFileChooser();
                        destFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                        destFile.setCurrentDirectory(new File(System.getProperty("user.home")));
                        destFile.showOpenDialog(this);
                        File selectedFile = destFile.getSelectedFile();
                        if (selectedFile != null) {

                            String destPathDownload = selectedFile.getAbsolutePath();

                            // kiểm tra đã chọn đường dẫn download chưa???
                            if (!destPathDownload.trim().equals("")) {
                                String fileName = tblFileShared.getValueAt(selectedRow, 1).toString();
                                String destinationPath = tblFileShared.getValueAt(selectedRow, 6).toString();
                                FileDownloadInfo fileDownloadInfo = new FileDownloadInfo();
                                fileDownloadInfo.setFileName(fileName);
                                fileDownloadInfo.setDestinationPath(FileExtensions.replaceBackslashes(destPathDownload) + "/");
                                fileDownloadInfo.setSourceFilePath(destinationPath);

                                ClientThread.request("download_file", fileDownloadInfo);
                                try {
                                    Thread thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            new LoadingDownloadFile();
                                        }
                                    });
                                    thread.start();
                                } catch (Exception e) {
                                }
//                                Message("Tải file xuống thành công.!!!");
                            } else {
                                Message("Vui lòng chọn đường dẫn tải xuống.!!!");
                            }
                        } else {
                            Message("Vui lòng chọn đường dẫn tải xuống.!!!");
                        }
                    } else {
                        Message("Bạn không có quyền tải xuống file này.!!!");
                    }
                }
            }
        } else {
            Message("Chức năng download của bạn đã bị chặn.!!!");
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void lblShareWithMeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblShareWithMeMouseClicked
        try {
            showDataMyFileShare();
            IS_UPLOAD_SHARE_WITH_ME = true;
        } catch (Exception ex) {
//            System.err.println("Xảy ra lỗi khi load data lên layout share with me" + ex);
        }

        // redirect view
        repaint();
        ClientThread.tranferLayout(pnlSection, "pnlShareWithMe");
    }//GEN-LAST:event_lblShareWithMeMouseClicked

    private void tblFolderSharedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFolderSharedMouseClicked
        int selectedRow = tblFolderShared.getSelectedRow();
        locationYourFolder = tblFolderShared.getValueAt(selectedRow, 3).toString();
    }//GEN-LAST:event_tblFolderSharedMouseClicked

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        Notification notifi = new Notification(this, rootPaneCheckingEnabled);
        notifi.show();
        loadCountNewNotification(0);
        TOTAL_NOTIFICATIONS = 0;
    }//GEN-LAST:event_jLabel8MouseClicked

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        shutdown();
    }//GEN-LAST:event_jLabel1MouseClicked

    private void jLabel30MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel30MouseClicked
        shutdown();
    }//GEN-LAST:event_jLabel30MouseClicked

    private void lblMinimizeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMinimizeMouseClicked
        this.setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_lblMinimizeMouseClicked

    private void jLabel23MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MouseClicked
        this.setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_jLabel23MouseClicked

    private void jLabel24MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MouseClicked
        this.setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_jLabel24MouseClicked

    private void lblReloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblReloadMouseClicked
        if (!userInfo.getEmail().trim().equals("anonymous")) {
            Users user = new Users();
            user.setEmail(userInfo.getEmail().trim());
            user.setPassword(userInfo.getPassword().trim());
            ClientThread.request("reload_with_authenticate", user);
            while (processHandler) {
                System.out.println("watting handler authenticate...");
            }

            if (statusResult) {
                lblSayHelloUser.setText("Xin chào: " + userInfo.getFullName());
                try {
                    showDataMyFileCloud();
                } catch (Exception ex) {
                }

                loadProcessMemory();

                // thiết lập đường dẫn root thư mục của client trên server
                // phục vụ cho upload/download file
                locationYourFolder = folderInfo.getFolderPath();
                folderSelected = folderInfo;
                prexEmailInfo = userInfo.getEmail().split("@")[0];

                if (userInfo.getAnonymousPermission().trim().equals("lock")) {
                    lblPublicCloud.setVisible(false);
                }
            }
        } else {
            ClientThread.request("reload_with_anonymous", "anonymous");

            while (processHandler) {
                System.out.println("watting handler authenticate...");
            }

            if (statusResult) {
                lblSayHelloUser.setText("Xin chào: " + userInfo.getFullName());
                try {
                    showDataPublicCloud();
                } catch (Exception ex) {
                }

                locationYourFolder = folderInfo.getFolderPath();
                folderSelected = folderInfo;
                prexEmailInfo = "anonymous";
                lblTitlePath.setText("Public Cloud ");
            }
        }

        // redirect view
        repaint();
        ClientThread.tranferLayout(pnlContainer, "pnlMain");
        ClientThread.tranferLayout(pnlSection, "pnlMyCloud");
        setDefaultProcessHandler();
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    new LoadingProcess();
                }
            });
            thread.start();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_lblReloadMouseClicked

    private void tblMyFileCloudMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMyFileCloudMouseReleased
            // TODO add your handling code here:
        if (evt.isPopupTrigger() && tblMyFileCloud.getSelectedRowCount() > 0) {
        int row = tblMyFileCloud.rowAtPoint(evt.getPoint());
        tblMyFileCloud.setRowSelectionInterval(row, row);

        JPopupMenu popup = new JPopupMenu();
        JMenuItem itemPreview = new JMenuItem("Xem file");

        itemPreview.addActionListener(e -> {
            Files file = listFileInfo.get(row);  // danh sách file tương ứng JTable
            String ext = file.getFileExtension().toLowerCase();

            if (ext.matches("txt|log|java|json|xml")) {
                ClientThread.request("PREVIEW_FILE", file);
            } else if (ext.matches("jpg|jpeg|png|gif")) {
                ClientThread.request("PREVIEW_IMAGE", file);
            } else if (ext.matches("pdf|docx")) {
                ClientThread.request("PREVIEW_DOC", file);
            } else {
                JOptionPane.showMessageDialog(this, "Không hỗ trợ định dạng này.");
            }
        });

            popup.add(itemPreview);
            popup.show(tblMyFileCloud, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tblMyFileCloudMouseReleased

    
    private void tblFileSharedMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFileSharedMouseReleased
        // TODO add your handling code here:
        if (evt.isPopupTrigger() && tblFileShared.getSelectedRowCount() > 0) {
            int row = tblFileShared.rowAtPoint(evt.getPoint());
            tblFileShared.setRowSelectionInterval(row, row);

            Files file = listFileShareInfo.get(row); // 👈 Lấy file từ danh sách chia sẻ

            JPopupMenu popup = new JPopupMenu();
            JMenuItem itemPreview = new JMenuItem("Xem file");

            itemPreview.addActionListener(e -> {
                String ext = file.getFileExtension().toLowerCase();
                if (ext.matches("txt|log|java|json|xml")) {
                    ClientThread.request("PREVIEW_FILE", file);
                } else if (ext.matches("jpg|jpeg|png|gif")) {
                    ClientThread.request("PREVIEW_IMAGE", file);
                } else if (ext.matches("pdf|docx")) {
                    ClientThread.request("PREVIEW_DOC", file);
                } else {
                    JOptionPane.showMessageDialog(this, "Định dạng không được hỗ trợ xem trước.");
                }
            });

            popup.add(itemPreview);
            popup.show(tblFileShared, evt.getX(), evt.getY());
        }  
    }//GEN-LAST:event_tblFileSharedMouseReleased
    



    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="SHUT DOWN">
    private void shutdown() {
        int value = JOptionPane.showConfirmDialog(this, "You will shutdown then click OK.!\nAre you sure.?", "Warnning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (value == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Thanks for using FTP Service.!!!", "Successfully", JOptionPane.INFORMATION_MESSAGE);
            ClientThread.disconnect();
        }
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Process tiến trình xử lý bắn request -> chờ response -> end">
    public static void processHandler(boolean status, String message) {
        processHandler = false;
        statusResult = status;
        messageResult = message;
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Thiết lập mặc định process tiến trình xử lý bắn request">
    private static void setDefaultProcessHandler() {
        processHandler = true;
        statusResult = false;
        messageResult = "";
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Thiết lập mã xác thực gửi về từ server">
    public static void setVerifyCode(int code) {
        verifyCode = code;
        timestamp = DateHelper.Now_Ver2();
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Return data khi handle login">
    public static void responseDataAfterAuthen(Users user, Folders folder,
                                               List<Folders> listFolderChild, List<Files> listFile,
                                               List<FileShares> listFileShared, List<FolderShares> listFolderShared,
                                               List<Files> listFileShares, List<Folders> listFolderShares,
                                               List<Permissions> listPermissions) {
        userInfo = user;
        folderInfo = folder;
        listFolderChildInfo = listFolderChild;
        listFileInfo = listFile;
        listFileSharedInfo = listFileShared;
        listFolderSharedInfo = listFolderShared;
        listFileShareInfo = listFileShares;
        listFolderShareInfo = listFolderShares;
        listPermissionInfo = listPermissions;
    }

    public static void responseDataAfterAuthen_Anonymous(Users user, Folders folder,
                                                         List<Files> listFile) {
        userInfo = user;
        folderInfo = folder;
        listFileInfo = listFile;

        listFolderChildInfo = new ArrayList<>();
    }
    // </editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Handle Sigin">
    private void handleSignIn() {
        Users user = new Users();
        user.setEmail(txtLoginEmail.getText());
        user.setPassword(Encryptions.md5(String.valueOf(txtLoginPass.getPassword())));
        
        ClientThread.request("authenticate", user);
        
        while (processHandler) {
            System.out.println("watting handler authenticate...");
            // do not something...
            // sẽ dừng khi hoàn tất tiến trình authenticate
        }
        Message(messageResult);
        if (statusResult) {
            lblSayHelloUser.setText("Xin chào: " + userInfo.getFullName());
            txtLoginEmail.setText("");
            txtLoginPass.setText("");
            try {
                showDataMyFileCloud();
            } catch (Exception ex) {
//                System.err.println("Đã bỏ qua func showDataMyFileCloud - " + ex);
            }

            loadProcessMemory();

            // thiết lập đường dẫn root thư mục của client trên server
            // phục vụ cho upload/download file
            locationYourFolder = folderInfo.getFolderPath();
            folderSelected = folderInfo;
            prexEmailInfo = userInfo.getEmail().split("@")[0];

            if (userInfo.getAnonymousPermission().trim().equals("lock")) {
                lblPublicCloud.setVisible(false);
            }

            // redirect view
            repaint();
            ClientThread.tranferLayout(pnlContainer, "pnlMain");
            ClientThread.tranferLayout(pnlSection, "pnlMyCloud");
        }
        setDefaultProcessHandler();
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Kiểm tra folder còn đủ dung lượng ko">
    private boolean isHaveEnoughSizeFolder(String remainingSizeFolder, String fileSize) {
        try {
            double remainingSizeFolderConvert = Double.parseDouble(remainingSizeFolder.replaceAll(",", ""));
            double fileSizeConvert = Double.parseDouble(fileSize.replaceAll(",", ""));
            return (fileSizeConvert <= remainingSizeFolderConvert);
        } catch (Exception ex) {
            return false;
        }
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Kiểm tra tên file khi upload đã tồn tại chưa">
    private void checkFileNameExist(Files fileInfo) {
//        String totalSize = folderInfo.getSize().replaceAll(",", "");
        try {
            String remainingSizeFolder = folderInfo.getRemainingSize();
            for (Files file : listFileInfo) {
                if (file.getFileName().equals(fileInfo.getFileName())
                        && file.getSourcePath().trim().equals(fileInfo.getSourcePath().trim())) {
                    Message("⚠ File trùng tên đã tồn tại. Hệ thống sẽ ghi đè thông tin.");
                    file.setFileSize(fileInfo.getFileSize());
                    file.setFileExtension(fileInfo.getFileExtension());
                    file.setUploadAt(fileInfo.getUploadAt());

                    // update lại dung lượng folder
                    double remainingSizeFolderConvert
                            = Double.parseDouble(remainingSizeFolder.replaceAll(",", ""))
                            + Double.parseDouble(file.getFileSize().replaceAll(",", ""))
                            - Double.parseDouble(fileInfo.getFileSize().replaceAll(",", ""));
                    folderInfo.setRemainingSize(String.valueOf(remainingSizeFolderConvert));
                    return;
                }
            }

            // chưa tồn tại tên file đó
            double remainingSizeFolderConvert
                    = Double.parseDouble(remainingSizeFolder.replaceAll(",", ""))
                    - Double.parseDouble(fileInfo.getFileSize().replaceAll(",", ""));

            folderInfo.setRemainingSize(String.valueOf(remainingSizeFolderConvert));
            listFileInfo.add(fileInfo);
        } catch (Exception ex) {
        }
    }
    //</editor-fold>

//     private boolean checkFolderPermissionUpload(String folderId) {
//         // Nếu folder là anonymous thì cho phép luôn
//         if ("anonymous".equalsIgnoreCase(folderId)) {
//             return true;
//         }

//         // Kiểm tra folderId được chọn
//         for (Folders folder : listFolderChildInfo) {
//             if (folder.getFolderId().equals(folderId)) {
//                 // Nếu chính folder đang bị khóa
//                 if (!"unlock".equalsIgnoreCase(folder.getFolderUserPermission())) {
//                     return false;
//                 }

//                 // Kiểm tra tất cả thư mục cha của nó trong danh sách
//                 String currentPath = folder.getFolderPath();
//                 for (Folders possibleParent : listFolderChildInfo) {
//                     if (!possibleParent.getFolderId().equals(folderId) &&
//                         currentPath.startsWith(possibleParent.getFolderPath())) {
//                         if (!"unlock".equalsIgnoreCase(possibleParent.getFolderUserPermission())) {
//                             return false; // Folder cha đang bị lock
//                         }
//                     }
//                 }
//                 return true; // Không có cha bị lock
//             }
//         }

//         // Nếu là folder gốc của user
//         if (folderInfo.getFolderId().equals(folderId)) {
//             return "unlock".equalsIgnoreCase(folderInfo.getFolderUserPermission());
//         }

//         return false;
// }


@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="handleUploadFile">
//     private void handleUploadFile() {
//         // check user có đc quyền upload ko???
//         if (userInfo.getPermissionId().trim().toLowerCase().equals("all")
//                 || userInfo.getPermissionId().trim().toLowerCase().equals("u")) {   // TH có
//             File file = FileExtensions.getFileChooser();    // open dialog and chọn file

//             // kiểm tra có chọn file chưa ???
//             if (file != null) {

//                 // kiểm tra kích thước file có đạt mức cho phép upload ko???
//                 if (FileHandler.compareFileSize(file, userInfo.getFileSizeUpload().trim())) {

//                     // quyền anonymous
//                     if (userInfo.getEmail().trim().equals("anonymous")) {

//                         @SuppressWarnings("unchecked")
//                         // <editor-fold defaultstate="collapsed" desc="Upload file với quyền anonymous">
//                         Files fileInfo = new Files();
//                         fileInfo.setFileId(ThreadRandoms.uuid());
//                         fileInfo.setFileName(FileExtensions.getFileName(file));
//                         fileInfo.setSourcePath(locationYourFolder);
//                         fileInfo.setFileSize(String.valueOf(FileExtensions.getFileSize(file.getAbsolutePath())));
//                         fileInfo.setFileExtension(FileExtensions.getFileExtension(file));
//                         fileInfo.setStatus("show");
//                         fileInfo.setFolderId(folderSelected.getFolderId());
//                         fileInfo.setUploadAt(DateHelper.Now());
//                         fileInfo.setPrexEmail(prexEmailInfo);

//                         ClientThread.requestFileSender("upload_file", fileInfo, file, locationYourFolder);

//                         try {
//                             boolean checkLoop = false;
//                             // cập nhật lại file nếu nó tồn tại rồi
//                             for (Files item : listFileInfo) {
//                                 if (item.getFileName().equals(fileInfo.getFileName())) {
//                                     item.setFileSize(fileInfo.getFileSize());
//                                     item.setFileExtension(fileInfo.getFileExtension());
//                                     item.setUploadAt(fileInfo.getUploadAt());
//                                     checkLoop = true;
//                                     break;
//                                 }
//                             }
//                             if (!checkLoop) {
//                                 listFileInfo.add(fileInfo);
//                             }
//                         } catch (Exception ex) {
// //                            System.err.println("Xảy ra lỗi khi add thông tin file vào list - " + ex);
//                         }

//                         try {
//                             // set up table show list file of user
//                             tblMyFileCloudModel.setRowCount(0);
//                             if (!listFileInfo.isEmpty()) {
//                                 listFileInfo.forEach((item) -> {
//                                     tblMyFileCloudModel.addRow(new Object[]{
//                                         item.getFileName().trim(),
//                                         item.getPrexEmail().trim(),
//                                         item.getUploadAt().trim(),
//                                         item.getFileExtension().trim(),
//                                         FileExtensions.convertSizeFromSizeString(item.getFileSize(), "MB") + " MB",
//                                         item.getFileSize().trim(),
//                                         item.getFolderId().trim()
//                                     });
//                                 });
//                             }
//                         } catch (Exception ex) {
// //                            System.err.println("Xảy ra lỗi khi load data của user lên UI" + ex);
//                         }
//                         try {
//                             Thread thread = new Thread(new Runnable() {
//                                 @Override
//                                 public void run() {
//                                     new LoadingUploadFile();
//                                 }
//                             });
//                             thread.start();
//                         } catch (Exception e) {
//                         }
// //                        Message("Tải file lên thành công.!!!");
//                         return;
//                         //</editor-fold>

//                     } else {

//                         // <editor-fold defaultstate="collapsed" desc="Upload file với quyền user">
//                         // kiểm tra folder đc chọn để upload file có được cấp quyền user chưa???
                        
//                         // if (folderSelected.getFolderUserPermission().trim().equals("unlock")) {   // đã đc cấp
//                         if (checkFolderPermissionUpload(folderSelected.getFolderId())) {
//                             // kiểm tra dung lượng folder còn đủ ko???
//                             if (isHaveEnoughSizeFolder(folderInfo.getRemainingSize(),
//                                     String.valueOf(FileExtensions.getFileSize(file.getAbsolutePath())))) {
//                                 Files fileInfo = new Files();
//                                 fileInfo.setFileId(ThreadRandoms.uuid());
//                                 fileInfo.setFileName(FileExtensions.getFileName(file));
//                                 fileInfo.setSourcePath(locationYourFolder);
//                                 fileInfo.setFileSize(String.valueOf(FileExtensions.getFileSize(file.getAbsolutePath())));
//                                 fileInfo.setFileExtension(FileExtensions.getFileExtension(file));
//                                 fileInfo.setStatus("show");
//                                 fileInfo.setFolderId(folderSelected.getFolderId());
//                                 fileInfo.setUploadAt(DateHelper.Now());
//                                 fileInfo.setPrexEmail(prexEmailInfo);

//                                 ClientThread.requestFileSender("upload_file", fileInfo, file, locationYourFolder);
                                
//                                 // Kiểm tra tên file khi upload đã tồn tại chưa???
//                                 checkFileNameExist(fileInfo);
//                                 showDataMyFileCloud();
//                                 loadProcessMemory();
//                                 try {
//                                     Thread thread = new Thread(new Runnable() {
//                                         @Override
//                                         public void run() {
//                                             new LoadingUploadFile();
//                                         }
//                                     });
//                                     thread.start();
//                                 } catch (Exception e) {
//                                 }
// //                                Message("Tải file lên thành công.!!!");
//                             } else {
//                                 Message("Không đủ dung lượng.\nVui lòng xóa bớt hoặc upload file với dung lượng nhỏ hơn");
//                             }
//                         } else {
//                             Message("Bạn không thể upload file vào thư mục này.\nVì bạn đã bị lock quyền user trong folder này.\nVui lòng quay lại sau.!!!");
//                         }

//                         // </editor-fold>
//                     }
//                 } else {
//                     Message("Kích thước file upload tối đa"
//                             + String.format("%,.2f", (Double.parseDouble(userInfo.getFileSizeUpload().trim().replaceAll(",", "")) / 1024))
//                             + "KB.!!!");
//                 }
//             }
//         } else {
//             Message("Chức năng upload file của bạn đã bị chặn.!!!");
//         }
//     }

    private void handleUploadFile() {
    // Kiểm tra quyền upload (all hoặc u)
        if (userInfo.getPermissionId().trim().toLowerCase().equals("all")
                || userInfo.getPermissionId().trim().toLowerCase().equals("u")) {

            if (folderSelected == null) {
                Message("Không tìm thấy thư mục được chọn để upload.!!!");
                return;
            }

            File file = FileExtensions.getFileChooser(); // Mở hộp thoại chọn file
            if (file == null) return;

            // Kiểm tra kích thước file hợp lệ
            if (!FileHandler.compareFileSize(file, userInfo.getFileSizeUpload().trim())) {
                Message("Kích thước file upload tối đa là "
                        + String.format("%,.2f", (Double.parseDouble(userInfo.getFileSizeUpload().trim().replaceAll(",", "")) / 1024))
                        + "KB.!!!");
                return;
            }

            // ✅ Anonymous upload
            if (userInfo.getEmail().trim().equalsIgnoreCase("anonymous")) {
                uploadFileToServer(file);
                return;
            }

            // ✅ Log kiểm tra
            System.out.println("[client] folderSelected = " + folderSelected.getFolderName());
            System.out.println("[client] folderSelected Path = " + folderSelected.getFolderPath());
            System.out.println("[client] Permission = " + folderSelected.getFolderUserPermission());

            // ✅ User thường: kiểm tra quyền + dung lượng
            String permission = folderSelected.getFolderUserPermission();
            System.out.println("[DEBUG] folderSelected Permission Raw = '" + permission + "'");
            System.out.println("[DEBUG] folderSelected Permission Length = " + (permission != null ? permission.length() : "null"));

            // if (!"unlock".equalsIgnoreCase(folderSelected.getFolderUserPermission())) {
            //     Message("Bạn không thể upload file vào thư mục này.\nVì bạn đã bị khóa quyền upload.");
            //     return;
            // }

            if (!"unlock".equalsIgnoreCase(permission.trim())) {
                Message("Bạn không thể upload file vào thư mục này.\nVì bạn đã bị khóa quyền upload.");
                return;
            }

            if (!isHaveEnoughSizeFolder(folderInfo.getRemainingSize(),
                    String.valueOf(FileExtensions.getFileSize(file.getAbsolutePath())))) {
                Message("Không đủ dung lượng.\nVui lòng xóa bớt hoặc upload file với dung lượng nhỏ hơn.");
                return;
            }

            // ✅ Nếu qua tất cả kiểm tra
            uploadFileToServer(file);

        } else {
            Message("Chức năng upload file của bạn đã bị chặn.!!!");
        }
    }


//</editor-fold>

    private void uploadFileToServer(File file) {
        Files fileInfo = new Files();
        fileInfo.setFileId(ThreadRandoms.uuid());
        fileInfo.setFileName(FileExtensions.getFileName(file));
        fileInfo.setSourcePath(locationYourFolder);
        fileInfo.setFileSize(String.valueOf(FileExtensions.getFileSize(file.getAbsolutePath())));
        fileInfo.setFileExtension(FileExtensions.getFileExtension(file));
        fileInfo.setStatus("show");
        fileInfo.setFolderId(folderSelected.getFolderId());
        fileInfo.setUploadAt(DateHelper.Now());
        fileInfo.setPrexEmail(prexEmailInfo);

        // Gửi yêu cầu upload tới server
        ClientThread.requestFileSender("upload_file", fileInfo, file, locationYourFolder);

        // ✅ Kiểm tra trùng tên → cập nhật nếu có, thêm mới nếu không
        checkFileNameExist(fileInfo);
        
        // ⏳ Gửi lại yêu cầu reload data từ server
        if (!userInfo.getEmail().trim().equalsIgnoreCase("anonymous")) {
            Users user = new Users();
            user.setEmail(userInfo.getEmail().trim());
            user.setPassword(userInfo.getPassword().trim());

            ClientThread.request("reload_with_authenticate", user);

            // Đợi server xử lý xong
            while (processHandler) {
                System.out.println("⏳ Waiting reload handler...");
            }

            // Nếu thành công → cập nhật UI
            if (statusResult) {
                showDataMyFileCloud();
                loadProcessMemory();

                // Cập nhật lại context người dùng
                locationYourFolder = folderInfo.getFolderPath();
                folderSelected = folderInfo;
                prexEmailInfo = userInfo.getEmail().split("@")[0];
            }

        } else {
            // Trường hợp anonymous
            ClientThread.request("reload_with_anonymous", "anonymous");

            while (processHandler) {
                System.out.println("⏳ Waiting reload handler...");
            }

            if (statusResult) {
                showDataPublicCloud();

                locationYourFolder = folderInfo.getFolderPath();
                folderSelected = folderInfo;
                prexEmailInfo = "anonymous";
            }
        }

        // 🧠 Gọi lại hiệu ứng loading
        try {
            Thread thread = new Thread(() -> new LoadingUploadFile());
            thread.start();
        } catch (Exception e) {}
        
    }



    // @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="handleUploadFileToFolderShare">
    private void handleUploadFileToFolderShare() {
        int selectedRow = tblFolderShared.getSelectedRow();
        if (selectedRow == -1) {
            Message("Vui lòng chọn thư mục chia sẻ cần upload.");
            return;
        }

        // Lấy quyền được chia sẻ: a (all) hoặc u (upload)
        String sharePermission = tblFolderShared.getValueAt(selectedRow, 5).toString().trim().toLowerCase();

        // ✅ B1: Kiểm tra quyền người nhận chia sẻ
        if (!sharePermission.equals("a") && !sharePermission.equals("u")) {
            Message("Bạn không có quyền upload vào thư mục chia sẻ này.");
            return;
        }

        // ✅ B2: Kiểm tra quyền người dùng hiện tại
        String userPermission = userInfo.getPermissionId().trim().toLowerCase();
        if (!userPermission.equals("all") && !userPermission.equals("u")) {
            Message("Tài khoản của bạn không được phép upload file.");
            return;
        }

        // ✅ B3: Mở hộp thoại chọn file
        File file = FileExtensions.getFileChooser();
        if (file == null) return;

        // ✅ B4: Kiểm tra kích thước hợp lệ
        if (!FileHandler.compareFileSize(file, userInfo.getFileSizeUpload().trim())) {
            Message("Kích thước file upload tối đa là "
                    + String.format("%,.2f", (Double.parseDouble(userInfo.getFileSizeUpload().trim().replaceAll(",", "")) / 1024)) + "KB.");
            return;
        }

        // ✅ B5: Kiểm tra dung lượng folder (folderInfo là của user, không liên quan tới folder share nhưng có thể tạm dùng nếu chưa tách logic rõ)
        if (!isHaveEnoughSizeFolder(folderInfo.getRemainingSize(),
                String.valueOf(FileExtensions.getFileSize(file.getAbsolutePath())))) {
            Message("Không đủ dung lượng.\nVui lòng xóa bớt hoặc upload file với dung lượng nhỏ hơn.");
            return;
        }

        // ✅ B6: Tạo thông tin file
        Files fileInfo = new Files();
        fileInfo.setFileId(ThreadRandoms.uuid());
        fileInfo.setFileName(FileExtensions.getFileName(file));
        fileInfo.setSourcePath(locationYourFolder);
        fileInfo.setFileSize(String.valueOf(FileExtensions.getFileSize(file.getAbsolutePath())));
        fileInfo.setFileExtension(FileExtensions.getFileExtension(file));
        fileInfo.setStatus("show");

        // Lấy folderId và chủ sở hữu từ dòng được chọn
        String folderId = tblFolderShared.getValueAt(selectedRow, 0).toString();
        String emailParent = tblFolderShared.getValueAt(selectedRow, 4).toString();  // email của người chia sẻ

        fileInfo.setFolderId(folderId);
        fileInfo.setUploadAt(DateHelper.Now());
        fileInfo.setPrexEmail(emailParent.split("@")[0]);  // dùng prexEmail của chủ sở hữu
        fileInfo.setEmailShare(userInfo.getEmail());       // email của người upload

        // ✅ Gửi request upload
        ClientThread.requestFileSender("upload_file_share", fileInfo, file, locationYourFolder);

        // ✅ Cập nhật list local
        boolean isExist = false;
        for (Files item : listFileShareInfo) {
            if (item.getFileName().trim().equals(fileInfo.getFileName().trim())
                    && item.getSourcePath().trim().equals(fileInfo.getSourcePath().trim())) {
                item.setFileSize(fileInfo.getFileSize());
                item.setFileExtension(fileInfo.getFileExtension());
                item.setUploadAt(fileInfo.getUploadAt());
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            listFileShareInfo.add(fileInfo);
        }

        // ✅ Cập nhật bảng
        showDataMyFileShare();

        try {
            Thread thread = new Thread(() -> new LoadingUploadFile());
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Add item vào list folder con và update lại cmb folder child">
    public void addFolderChildToListFolderChild(Folders folder) {
        listFolderChildInfo.add(folder);

        // update list folder child of user
        cmbFolderChild.removeAllItems();
        cmbFolderChild.addItem("--Choose a folder child--");
        listFolderChildInfo.forEach((folderChild) -> {
            String folderPath = folderChild.getFolderPath().substring(folderInfo.getFolderPath().length());
            cmbFolderChild.addItem(folderPath);
        });
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Load số lượng thông báo mới">
    public static void loadCountNewNotification(int sum) {
        lblTotalNotification.setText(String.valueOf(sum));
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Hiển thị modal thông báo">
    private void Message(String message) {
        JOptionPane.showMessageDialog(rootPane, message);
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Ẩn cột đầu tiên của table">
    private void hiddenColumnFirst(JTable table) {
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
    }

    private void hiddenColumn(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        table.getColumnModel().getColumn(columnIndex).setWidth(0);
    }
    // </editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Main Runable">
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientUI().setVisible(true);
            }
        });
    }
    // </editor-fold> 

    @SuppressWarnings("unchecked")
// <editor-fold defaultstate="collapsed" desc="Rounded">
    class RoundedPanel extends JPanel {

        private Color backgroundColor;
        private int cornerRadius = 15;

        public RoundedPanel(int radius) {
            super();
            cornerRadius = radius;
        }

        public RoundedPanel(LayoutManager layout, int radius) {
            super(layout);
            cornerRadius = radius;
        }

        public RoundedPanel(int radius, Color bgColor) {
            super();
            cornerRadius = radius;
            backgroundColor = bgColor;
        }

        public RoundedPanel(LayoutManager layout, int radius, Color bgColor) {
            super(layout);
            cornerRadius = radius;
            backgroundColor = bgColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponents(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draws the rounded panel with borders
            if (backgroundColor != null) {
                graphics.setColor(backgroundColor);
            } else {
                graphics.setBackground(getBackground());
            }
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcs.width, arcs.height);
            graphics.setColor(getForeground());
        }
    }

    class RoundedLable extends JLabel {

        private Color backgroundColor;
        private int cornerRadius = 15;

        public RoundedLable(int radius) {
            super();
            cornerRadius = radius;
        }

        public RoundedLable(int radius, Color bgColor) {
            super();
            cornerRadius = radius;
            backgroundColor = bgColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponents(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draws the rounded panel with borders
            if (backgroundColor != null) {
                graphics.setColor(backgroundColor);
            } else {
                graphics.setBackground(getBackground());
            }
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcs.width, arcs.height);
            graphics.setColor(getForeground());
        }
    }

    class RoundedSignIn extends JLabel {

        private Color backgroundColor;
        private int cornerRadius = 15;

        public RoundedSignIn(int radius) {
            super();
            cornerRadius = radius;
        }

        public RoundedSignIn(int radius, Color bgColor) {
            super();
            cornerRadius = radius;
            backgroundColor = bgColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponents(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draws the rounded panel with borders
            if (backgroundColor != null) {
                graphics.setColor(backgroundColor);
            } else {
                graphics.setBackground(getBackground());
            }
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcs.width, arcs.height);
            graphics.setColor(getForeground());
            graphics.drawString("Sign In", 37, 26);
        }
    }

    class RoundedRegister extends JLabel {

        private Color backgroundColor;
        private int cornerRadius = 15;

        public RoundedRegister(int radius) {
            super();
            cornerRadius = radius;
        }

        public RoundedRegister(int radius, Color bgColor) {
            super();
            cornerRadius = radius;
            backgroundColor = bgColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponents(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draws the rounded panel with borders
            if (backgroundColor != null) {
                graphics.setColor(backgroundColor);
            } else {
                graphics.setBackground(getBackground());
            }
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcs.width, arcs.height);
            graphics.setColor(getForeground());
            graphics.drawString("Register", 35, 23);
        }
    }
// </editor-fold> 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel background;
    public static javax.swing.JProgressBar bar_memories;
    public static javax.swing.JButton btnCreateNewFolder;
    private javax.swing.JButton btnDownload;
    public static javax.swing.JButton btnFolderRoot;
    private javax.swing.ButtonGroup btnGroup_Sex;
    public static javax.swing.JButton btnShare;
    private javax.swing.JButton btnSignIn_Anonymous;
    private javax.swing.JCheckBox cbRemember;
    private javax.swing.JCheckBox cboRegis_FeMale;
    private javax.swing.JCheckBox cboRegis_Male;
    public static javax.swing.JComboBox<String> cmbFolderChild;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    public javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel4;
    public static javax.swing.JLabel jLabel5;
    public javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JTextField jTextField1;
    private com.toedter.calendar.JDateChooser jdtRegis_dbo;
    public static javax.swing.JLabel lblLoading_Register;
    private javax.swing.JLabel lblMinimize;
    public static javax.swing.JLabel lblMyCloud;
    public static javax.swing.JLabel lblPublicCloud;
    private javax.swing.JLabel lblRegisToLogin;
    private javax.swing.JLabel lblRegister;
    private javax.swing.JLabel lblReload;
    private javax.swing.JLabel lblSayHelloUser;
    public static javax.swing.JLabel lblShareWithMe;
    private javax.swing.JLabel lblSignIn;
    private javax.swing.JLabel lblSignout;
    public static javax.swing.JLabel lblTitleCmbFolderChild;
    public static javax.swing.JLabel lblTitlePath;
    private javax.swing.JLabel lblTitlePath1;
    public static javax.swing.JLabel lblTotalNotification;
    private javax.swing.JLabel lblUploadNewFile;
    private javax.swing.JLabel lblVerifyInfo;
    public static javax.swing.JPanel pnlContainer;
    public static javax.swing.JPanel pnlLogin;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlMyCloud;
    private javax.swing.JPanel pnlRegister;
    public static javax.swing.JPanel pnlSection;
    private javax.swing.JPanel pnlShareWithMe;
    private javax.swing.JTable tblFileShared;
    private javax.swing.JTable tblFolderShared;
    private javax.swing.JTable tblMyFileCloud;
    private javax.swing.JTextField txtLoginEmail;
    private javax.swing.JPasswordField txtLoginPass;
    private javax.swing.JTextField txtRegis_Email;
    private javax.swing.JTextField txtRegis_FullName;
    private javax.swing.JPasswordField txtRegis_Pass;
    private javax.swing.JTextField txtVerifyCode;
    // End of variables declaration//GEN-END:variables
}
