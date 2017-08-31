package works.weave.socks.shipping.controllers.access;

import com.cloud.sdk.http.HttpMethodName;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public abstract class AccessServiceShipping
{

    protected String serviceName = null;

    protected String region = null;

    protected String ak = null;

    protected String sk = null;

    public AccessServiceShipping(String serviceName, String region, String ak, String sk)
    {

        this.region = region;
        this.serviceName = serviceName;
        this.ak = ak;
        this.sk = sk;
    }

    /**
     * Access DMS Service,create trust connection
     *
     * @param url                The DMS Request full URL
     * @param header            Http request header
     * @param content            The sending content to DMS service server
     * @param contentLength        The length of sending content
     * @param httpMethod        The method type http: like get, put, delete, post.
     * @return Response Http content
     */
    public abstract HttpResponse access(URL url, Map<String, String> header, InputStream content, Long contentLength,
            HttpMethodName httpMethod)
            throws Exception;

    /**
     * Access DMS Service,create trust connection
     *
     * @param url                The DMS Request full URL
     * @param header            Http request header
     * @param httpMethod        The method type http: like get, put, delete, post.
     * @return Response Http content
     */
    public HttpResponse access(URL url, Map<String, String> header, HttpMethodName httpMethod)
            throws Exception
    {
        return this.access(url, header, null, 0l, httpMethod);
    }

    /**
     * @param url                The DMS Request full URL
     * @param content            The sending content to DMS service server
     * @param contentLength        The length of sending content
     * @param httpMethod        The method type http: like get, put, delete, post.
     * @return Response Http content
     *
     */
    public HttpResponse access(URL url, InputStream content, Long contentLength, HttpMethodName httpMethod)
            throws Exception
    {
        return this.access(url, null, content, contentLength, httpMethod);
    }

    /**
     *
     * @param url                The DMS Request full URL
     * @param httpMethod        The method type http: like get, put, delete, post.
     * @return Response Http content
     */
    public HttpResponse access(URL url, HttpMethodName httpMethod)
            throws Exception
    {
        return this.access(url, null, null, 0l, httpMethod);
    }

    public abstract void close();

    public String getServiceName()
    {
        return serviceName;
    }

}
