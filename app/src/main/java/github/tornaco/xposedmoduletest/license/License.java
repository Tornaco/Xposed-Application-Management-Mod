package github.tornaco.xposedmoduletest.license;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public class License {

    private String email, source;
    private long activeDate, expireDate;

    public License(String email, String source, long activeDate, long expireDate) {
        this.email = email;
        this.source = source;
        this.activeDate = activeDate;
        this.expireDate = expireDate;
    }

    public License() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getActiveDate() {
        return activeDate;
    }

    public void setActiveDate(long activeDate) {
        this.activeDate = activeDate;
    }

    public long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }

    @Override
    public String toString() {
        return "License{" +
                "email='" + email + '\'' +
                ", source='" + source + '\'' +
                ", activeDate=" + activeDate +
                ", expireDate=" + expireDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        License license = (License) o;

        if (!email.equals(license.email)) return false;
        return source.equals(license.source);

    }

    @Override
    public int hashCode() {
        int result = email.hashCode();
        result = 31 * result + source.hashCode();
        return result;
    }
}
