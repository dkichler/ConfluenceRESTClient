package com.softwareleaf.confluence.rest;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.softwareleaf.confluence.rest.model.*;
import com.softwareleaf.confluence.rest.util.Expand;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * A class that is capable of making requests to the confluence API.
 * Example Usage:
 * <pre>{@code
 *     String myURL = "http://confluence.organisation.org";
 *     String username = "...";
 *     String password = "...";
 *     ConfluenceClient client = ConfluenceClient.builder()
 *          .baseURL(myURL)
 *          .username(username)
 *          .password(password)
 *          .build();
 *     // search confluence instance by title and space key
 *     ContentResultList search =
 *     confluenceClient.getContentBySpaceKeyAndTitle("DEV", "A page or blog in DEV");
 * }</pre>
 *
 * @author Jonathon Hope
 */
public class ConfluenceClient {
    /**
     * The default base url is the production confluence instance.
     */
    public static final String BASE_URL = "http://localhost:8090";
    // default account credentials
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    /**
     * the Logger instance used by this class.
     */
    private static final Logger logger = Logger.getLogger(ConfluenceClient.class.getName());

    /**
     * The ConfluenceAPI endpoint.
     */
    private ConfluenceAPI confluenceAPI;

    /**
     * Constructor.
     */
    private ConfluenceClient(Builder builder) {
        this.confluenceAPI = builder.confluenceAPI;
    }

    /**
     * Fetch a single piece of content.
     *
     * @param id the id of the page or blog post to fetch.
     * @return the Content instance.
     */
    public Call<Content> getContentById(String id, Expand expand) {
        return confluenceAPI.getContentById(id, ImmutableMap.of("expand", expand.toQueryParams()));
    }

    public Call<Content> getContentById(String id) {
        return confluenceAPI.getContentById(id,
                ImmutableMap.of("expand", new Expand().nestedExpand(Expandable.BODY, Expandable.STORAGE).toQueryParams()));
    }

    /**
     * Fetch a results object containing a paginated list of content.
     *
     * @return an instance of {@code getContentResults} wrapping the list
     * of {@code Content} instances obtained from the API call.
     */
    public Call<ContentResultList> getContentResults() {
        return confluenceAPI.getContentResults();
    }

    /**
     * Perform a search for content, by space key and title.
     *
     * @param key   the space key to search under.
     * @param title the title of the piece of content to search for.
     * @return an instance of {@code getContentResults} wrapping the list
     * of {@code Content} instances obtained from the API call.
     */
    public Call<ContentResultList> getContentBySpaceKeyAndTitle(final String key, final String title) {
        return confluenceAPI.getContent(ImmutableMap.of("key", key, "title", title));
    }

    public Call<ContentResultList> getContentBySpaceKeyAndTitle(final String key, final String title, final Expand expand) {
        return confluenceAPI.getContent(ImmutableMap.of("key", key, "title", title,
                "expand", expand.toQueryParams()));
    }

    /**
     * Used for converting the storage format of a piece of content.
     *
     * @param storage   the storage instance to convert.
     * @param convertTo the representation to convert to.
     * @return an instance of {@code Storage} that contains the result of
     * the conversion request.
     * @see <a href="https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format">
     * Confluence Storage Format</a>
     */
    public Call<Storage> convertContent(final Storage storage, final Storage.Representation convertTo) {
        return confluenceAPI.postContentConversion(storage, convertTo.toString());
    }

    /**
     * Performs a POST request with the body of the request containing the
     * {@code content}, thus creating a new page or blog post on confluence.
     *
     * @param content the content to post to confluence.
     * @return the result {@code Content} instance with the {@code id} field updated.
     */
    public Call<Content> createContent(final Content content) {
        return confluenceAPI.postContent(content);
    }

    /**
     * Performs a PUT request with the body of the request containing the
     * {@code content}, thus updating page or blog post on confluence.
     *
     * @param content the content to put to confluence
     * @return //TODO
     */
    public Call<Content> udpateContent(final Content content) {
        return confluenceAPI.updateContent(content, content.getId());
    }

    /**
     * DELETE Content
     * <p>Trashes or purges a piece of Content, based on its {@literal ContentType} and
     * {@literal ContentStatus}.
     *
     * @param id the id of the page of blog post to be deleted.
     */
    public Call<NoContent> deleteContentById(final String id) {
        return confluenceAPI.deleteContentById(id);
    }

    /**
     * Obtain a list of available spaces.
     *
     * @return a list of spaces available on confluence.
     */
    public Call<SpaceResultList> getSpaces() {
        return confluenceAPI.getSpaces();
    }

    /**
     * Creates a new Confluence {@code Space} using {@code key} and
     * {@code name} of the given {@code space}.
     *
     * @param space the {@code Space} to create.
     * @return the {@code Space} as a confirmation returned by Confluence
     * REST API.
     */
    public Call<Space> createSpace(final Space space) {
        return confluenceAPI.createSpace(space);
    }

    /**
     * Creates a new private Space, viewable only by the Confluence User
     * account used by this {@code ConfluenceClient}.
     *
     * @param space the {@code Space} to create.
     * @return the {@code Space} as a confirmation returned by Confluence
     * REST API.
     */
    public Call<Space> createPrivateSpace(final Space space) {
        return confluenceAPI.createPrivateSpace(space);
    }

    public Call<ContentResultList> getRootContent(final String spaceKey) {
        return confluenceAPI.getSpaceContent(spaceKey, ImmutableMap.of(
                "depth", Depth.ROOT.toString()
        ));
    }

    public Call<ContentResultList> getRootContent(final String spaceKey, Expand expand) {
        return confluenceAPI.getSpaceContent(spaceKey, ImmutableMap.of(
                "depth", Depth.ROOT.toString(),
                "expand", expand.toQueryParams()
        ));
    }

    /**
     * Obtain a list of root content of a space.
     *
     * @param spaceKey    the space key of the Space.
     * @param contentType the type of content to return.
     * @return a list of Content instances obtained from the root.
     */
    public Call<ContentResultList> getSpaceRootContent(final String spaceKey, final Type contentType) {
        return confluenceAPI.getSpaceContent(spaceKey, contentType.toString(), ImmutableMap.of(
                "depth", Depth.ROOT.toString()
        ));
    }

    public Call<ContentResultList> getSpaceRootContent(final String spaceKey, final Type contentType, Expand expand) {
        return confluenceAPI.getSpaceContent(spaceKey, contentType.toString(), ImmutableMap.of(
                "depth", Depth.ROOT.toString(),
                "expand", expand.toQueryParams()
        ));
    }

    /**
     * Fetch all content from a confluence space.
     *
     * @param spaceKey the key that identifies the target Space.
     * @return a list of all content in the given Space identified by {@code spaceKey}.
     */
    public Call<ContentResultList> getSpaceContent(final String spaceKey) {
        return confluenceAPI.getSpaceContent(spaceKey, ImmutableMap.of(
                "expand", new Expand()
                        .expand(Expandable.ANCESTORS)
                        .nestedExpand(Expandable.BODY, Expandable.STORAGE)
                        .toQueryParams(),
                "limit", "1000"));
    }

    public Call<ContentResultList> getSpaceContent(final String spaceKey, int start, int limit) {
        return confluenceAPI.getSpaceContent(spaceKey, ImmutableMap.of(
                "expand", new Expand().expand(Expandable.ANCESTORS).nestedExpand(Expandable.BODY, Expandable.STORAGE).toQueryParams(),
                "start", String.valueOf(start),
                "limit", String.valueOf(limit)
        ));
    }

    public Call<ContentResultList> getSpaceContent(final String spaceKey, Expand expand) {
        return confluenceAPI.getSpaceContent(spaceKey, ImmutableMap.of(
                "expand", expand.toQueryParams(),
                "limit", "1000"));
    }

    public Call<ContentResultList> getSpaceContent(final String spaceKey, Expand expand, int start, int limit) {
        return confluenceAPI.getSpaceContent(spaceKey, ImmutableMap.of(
                "expand", expand.toQueryParams(),
                "start", String.valueOf(start),
                "limit", String.valueOf(limit)
        ));
    }

    public Call<ContentResultList> getSpaceContent(final String spaceKey, final Type contentType) {
        return confluenceAPI.getSpaceContent(spaceKey, contentType.toString(), ImmutableMap.of(
                "expand", new Expand()
                        .expand(Expandable.ANCESTORS)
                        .nestedExpand(Expandable.BODY, Expandable.STORAGE)
                        .toQueryParams(),
                "limit", "1000"));
    }

    public Call<ContentResultList> getSpaceContent(final String spaceKey, final Type contentType, int start, int limit) {
        return confluenceAPI.getSpaceContent(spaceKey, contentType.toString(), ImmutableMap.of(
                "expand", new Expand()
                        .expand(Expandable.ANCESTORS)
                        .nestedExpand(Expandable.BODY, Expandable.STORAGE)
                        .toQueryParams(),
                "start", String.valueOf(start),
                "limit", String.valueOf(limit)
        ));
    }

    public Call<ContentResultList> getSpaceContent(final String spaceKey, final Type contentType, Expand expand) {
        return confluenceAPI.getSpaceContent(spaceKey, contentType.toString(), ImmutableMap.of(
                "limit", "1000",
                "expand", expand.toQueryParams()));
    }

    public Call<ContentResultList> getSpaceContent(final String spaceKey, final Type contentType, Expand expand, int start, int limit) {
        return confluenceAPI.getSpaceContent(spaceKey, contentType.toString(), ImmutableMap.of(
                "expand", expand.toQueryParams(),
                "start", String.valueOf(start),
                "limit", String.valueOf(limit)
        ));
    }

    /**
     * Fetch the children for a given {@code Content} identified
     * by the {@code parentId}.
     *
     * @param parentId    the {@code id} of the parent {@code Content}.
     * @param contentType the {@code Type} of {@code Content}.
     * @return a list of all child content, matching the {@code content}
     * with the given {@code parentId}.
     */
    public Call<ContentResultList> getChildren(final String parentId, final Type contentType) {
        return confluenceAPI.getChildren(parentId, contentType.toString(), ImmutableMap.of(
                "limit", "1000"));
    }

    public Call<ContentResultList> getChildren(final String parentId, final Type contentType, Expand expand) {
        return confluenceAPI.getChildren(parentId, contentType.toString(), ImmutableMap.of(
                "limit", "1000",
                "expand", expand.toQueryParams()
        ));
    }

    public Call<ContentResultList> getChildren(final String parentId) {
        return confluenceAPI.getChildren(parentId, ImmutableMap.of(
                "limit", "1000"));
    }

    public Call<ContentResultList> getChildren(final String parentId, Expand expand) {
        return confluenceAPI.getChildren(parentId, ImmutableMap.of(
                "limit", "1000",
                "expand", expand.toQueryParams()
        ));
    }

    /**
     * Factory object for chaining the construction of a {@code ConfluenceClient}.
     *
     * @return an instance of the internal Builder class.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A Builder factory for implementing the Builder Pattern.
     */
    public static class Builder {
        /**
         * This is the reference to the concrete REST API Client generated by Retrofit.
         */
        private ConfluenceAPI confluenceAPI;
        /**
         * The username forms the first part of the credential used to authenticate requests.
         */
        private String username;
        /**
         * The password forms the second part of the credential used to authenticate requests.
         */
        private String password;
        /**
         * By default, {@link #BASE_URL} will be used as the url of the confluence instance; when
         * this is set, requests will be made to this base URL instead.
         */
        private String alternativeBaseURL;

        /**
         * By default the standard Retrofit {@code Client} will be used. However, if this is
         * {@link #supplyClient(OkHttpClient) set} then the provided {@code Client} will be used.
         */
        private OkHttpClient client;

        // prevent direct instantiation by external classes.
        private Builder() {
        }

        /**
         * Set the username parameter.
         *
         * @param username the username.
         * @return {@code this}.
         */
        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        /**
         * Sets the password parameter.
         *
         * @param password the password matching the username.
         * @return {@code this}.
         */
        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        /**
         * By default, {@link #BASE_URL BaseURL} will be used as the url of the confluence instance; when
         * this is set, requests will be made to this base URL instead.
         *
         * @param url the alternative Base url of the confluence instance to make requests to.
         * @return {@code this}.
         */
        public Builder baseURL(final String url) {
            this.alternativeBaseURL = url;
            return this;
        }

        /**
         * This provides a way for users to supply their own implementation of the underlying
         * {@link OkHttpClient}. For example, to use {@link OkHttpClient}
         * within a proxy environment:
         * <pre>{@code
         *  // example proxy setup
         *  final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.1", 8080));
         *  // setup your client to use the proxy
         *  final OkHttpClient httpClient = new OkHttpClient();
         *  httpClient.setProxy(proxy);
         *  // finally build the ConfluenceClient
         *  ConfluenceClient.builder()
         *      // other methods omitted for brevity...
         *      .supplyClient(httpClient)
         *      .build();
         * }</pre>
         *
         * @param client the {@link OkHttpClient} to use.
         * @return {@code this}.
         */
        public Builder supplyClient(final OkHttpClient client) {
            this.client = client;
            return this;
        }

        /**
         * Build and return a configured ConfluenceClient instance.
         *
         * @return a configured {@code ConfluenceClient} instance.
         */
        public ConfluenceClient build() {
            // configure the underlying rest adapter used to create our Confluence API service.
            final Retrofit retrofit = configureRetrofit();
            // Create an implementation of the API defined by the specified ConfluenceAPI interface
            this.confluenceAPI = retrofit.create(ConfluenceAPI.class);
            return new ConfluenceClient(this);
        }

        /**
         * Configures and builds the {@code RestAdapter} used to create the
         * {@code ConfluenceClient}.
         */
        private Retrofit configureRetrofit() {
            // determine if we are using the production confluence or not.
            final String URL = alternativeBaseURL == null ? BASE_URL : alternativeBaseURL;
            // determine the user credentials to use.
            final String username = this.username == null ? DEFAULT_USERNAME : this.username;
            final String password = this.password == null ? DEFAULT_PASSWORD : this.password;
            /*
             * The Confluence REST API requires HTTP Basic authentication using a
             * username and password pair, for a given Confluence user.
             * We therefore need to first encode the credentials using a Base64 encoder
             * and set up an interceptor that adds the requisite HTTP headers to each request.
             */
            final String credentials = username + ":" + password;
            // encode in base64.
            final String encodedCredentials = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

            // build the default RestAdapter
            final Retrofit.Builder retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(
                            GsonConverterFactory
                                    .create(new GsonBuilder()
                                            // handles confluence Date format
                                            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                            // ensures body.storage HTML is not escaped
                                            .disableHtmlEscaping()
                                            .create()));
            if (client == null) {
                client = new OkHttpClient();
            }
            client = client.newBuilder().addInterceptor(new BasicAuthenticationInterceptor(encodedCredentials)).build();
            retrofit.client(client);
            return retrofit.build();
        }

    }

    private static class BasicAuthenticationInterceptor implements Interceptor {

        private final String encodedCredentials;

        private BasicAuthenticationInterceptor(String encodedCredentials) {
            this.encodedCredentials = encodedCredentials;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            return chain.proceed(chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", encodedCredentials)
                    .build());
        }
    }

}
