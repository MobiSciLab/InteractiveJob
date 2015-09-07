package msl.com.httpclient;


public interface IRegApis {
    final String SERVER_NAME = "api-staging.vietnamworks.com:443";
    public String URL = "https://" + SERVER_NAME;
    public String LOGIN_URL = URL + "/users/login";
    public String SEARCH_URL = URL + "/jobs/search";
    public String GENERAL_CONFIG_URL = URL + "/general/configuration/data";
    public String SUBSCRIPTION_URL = URL + "/users/create-jobalert";
    public String EDIT_URL = URL + "/users/edit/token";
    public String SIGNUP_NO_CONFIRM_URL = URL + "/users/register";


    public String VNWORK_HEADER = "CONTENT-MD5";
    public String VNWORK_HEADER_VALUE = "017c8fbe29cc15972401579a81861da47a57b6f0a67aa0782c26436cdc0338a4";


    public void signupNoConfirm(String username, String password, String firstname, String lastname, String birthday, int gender, int nationality, int city, String homephone, String cellphone, int language);
    public void login(String username, String password);
    public void searchJob(String title);
    public void getGeneralConfig();
    public void subscribe(String username, String keywords, Object[] categories, Object[] locations, Object[] levels, String minSalary, String frequency, int language);
    public void editProfile(String token, String firstname, String lastname, String birthday, int gender, int nationality, int city, String homephone, String cellphone, int language);

}
