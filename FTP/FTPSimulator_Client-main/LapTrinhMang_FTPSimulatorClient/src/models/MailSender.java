package models;

public class MailSender {

    private final String FromEmail = "laptrinhmang33@gmail.com";
    private final String Password = "qfghhhhysaxltfwe";
    private final String Subject = "Verify Code";
    private final String AliasAddress = "FCloud";
    private final String Body = "Mã xác thực của bạn là: ";
    private final String ToEmail;

    public MailSender(String ToEmail) {
        this.ToEmail = ToEmail;
    }

    public String getFromEmail() {
        return FromEmail;
    }

    public String getPassword() {
        return Password;
    }

    public String getToEmail() {
        return ToEmail;
    }

    public String getSubject() {
        return Subject;
    }

    public String getBody() {
        return Body;
    }

    public String getAliasAddress() {
        return AliasAddress;
    }

}
