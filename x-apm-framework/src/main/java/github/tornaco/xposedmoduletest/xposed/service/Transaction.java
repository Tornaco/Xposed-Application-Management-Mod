package github.tornaco.xposedmoduletest.xposed.service;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class Transaction {
    public VerifyListener listener;
    public int uid;
    public int pid;
    int tid;
    public String pkg;

    public Transaction(VerifyListener listener, int uid, int pid, int tid, String pkg) {
        this.listener = listener;
        this.uid = uid;
        this.pid = pid;
        this.tid = tid;
        this.pkg = pkg;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "uid=" + uid +
                ", pid=" + pid +
                ", tid=" + tid +
                ", pkg='" + pkg + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        return tid == that.tid && pkg.equals(that.pkg);
    }

    @Override
    public int hashCode() {
        int result = tid;
        result = 31 * result + pkg.hashCode();
        return result;
    }
}