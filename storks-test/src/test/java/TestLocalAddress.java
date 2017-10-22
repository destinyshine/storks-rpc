import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Created by liujianyu.ljy on 17/8/24.
 *
 * @author liujianyu.ljy
 * @date 2017/08/24
 */
public class TestLocalAddress {

    public static void main(String[] args) throws UnknownHostException {
       System.out.println(Inet4Address.getLocalHost().getCanonicalHostName()) ;
    }
}
