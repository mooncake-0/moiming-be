package com.peoplein.moiming.config;


// 통합 URL 관리 클래스
public abstract class AppUrlPath {


    public final static String API_SERVER = "/api";

    // 큰 API DOMAIN
    public final static String API_DOMAIN_AUTH = "/auth";
    public final static String API_DOMAIN_MOIM = "/moim";
    public final static String API_DOMAIN_MEMBER = "/member";


    public final static String API_AUTH_VER = "/v0";


    public final static String API_MOIM_VER = "/v0";
    public final static String API_MOIM_MEMBER = "/member";
    public final static String API_MOIM_POST = "/post";
    public final static String API_POST_COMMENT = "/comment";


    public final static String API_MEMBER_VER = "/v0";
    public final static String API_MEMBER_POLICY = "/policy";


    public final static String API_DOMAIN_SEARCH = "/search";
    public final static String API_SEARCH_VER = "/v0";


    // Search 관련 API
    // /api/v0/search/moim?keyword={}&sortBy={}&areaFilter{}&categoryFilter={}&offset={}&limit={}
    public final static String PATH_SEARCH_MOIM = API_SERVER + API_SEARCH_VER + API_DOMAIN_SEARCH + "/moim";


    // Auth 관련 API
    // GET - /api/v0/auth/available/{email}
    public final static String PATH_AUTH_EMAIL_AVAILABLE = API_SERVER + API_AUTH_VER + API_DOMAIN_AUTH + "/available/{email}";
    // POST - /api/v0/auth/signup
    public final static String PATH_AUTH_SIGN_IN = API_SERVER + API_AUTH_VER + API_DOMAIN_AUTH + "/signup";
    // POST - /api/v0/auth/login
    public final static String PATH_AUTH_LOGIN = API_SERVER + API_AUTH_VER + API_DOMAIN_AUTH + "/login";
    // POST - /api/v0/auth/token
    public final static String PATH_AUTH_REISSUE_TOKEN = API_SERVER + API_AUTH_VER + API_DOMAIN_AUTH + "/token";
    public final static String PATH_AUTH_REQ_SMS_VERIFY = API_SERVER + API_AUTH_VER + API_DOMAIN_AUTH + "/sms" + "/send";
    public final static String PATH_AUTH_FIND_MEMBER_EMAIL = API_SERVER + API_AUTH_VER + API_DOMAIN_AUTH + "/requestEmail";
    public final static String PATH_AUTH_RESET_PW_CONFIRM = API_SERVER + API_AUTH_VER + API_DOMAIN_AUTH + "/confirmResetPw";
    public final static String PATH_AUTH_RESET_PW = API_SERVER + API_AUTH_VER + API_DOMAIN_AUTH + "/resetPw";




    // Moim 관련 API
    // POST - /api/v0/moim
    public final static String PATH_MOIM_CREATE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM;
    // GET - /api/v0/moim // TODO :: PAGING 정보 추가 필요
    public final static String PATH_MOIM_GET_VIEW = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM;
    // GET - /api/v0/moim/{moimId}
    public final static String PATH_MOIM_GET_DETAIL = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/{moimId}";
    // PATCH - /api/v0/moim
    public final static String PATH_MOIM_UPDATE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM;
    // PATCH - /api/v0/moim/joinRule
    public final static String PATM_MOIM_JOIN_RULE_UPDATE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/joinRule";
    // DELETE - /api/v0/moim/{moimId}
    public final static String PATH_MOIM_DELETE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/{moimId}";
    public final static String PATH_MOIM_FIXED_VALUES = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/fixed";
    public final static String PATH_MOIM_SUGGESTED = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/main" + "/suggest";


    // Moim Member 관련 API
    // POST - /api/v0/moim/member/join
    public final static String PATH_MOIM_MEMBER_JOIN = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + API_MOIM_MEMBER + "/join";
    // POST - /api/v0/moim/member/leave
    public final static String PATH_MOIM_MEMBER_LEAVE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + API_MOIM_MEMBER + "/leave";
    // POST - /api/v0/moim/member/expel
    public final static String PATH_MOIM_MEMBER_EXPEL = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + API_MOIM_MEMBER + "/expel";
    // GET - /api/v0/moim/{moimId}/member
    public final static String PATH_MOIM_MEMBER_GET_VIEW = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/{moimId}" + API_MOIM_MEMBER;



    // Moim Post 관련 API
    // GET - /api/v0/moim/{moimId}/post?lastPostId={id}&category={category}&limit={val}
    public final static String PATH_MOIM_POST_GET_VIEW = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/{moimId}" + API_MOIM_POST;
    public final static String PATH_MOIM_POST_GET_DETAIL = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/{moimId}" + API_MOIM_POST + "/{moimPostId}";
    // POST - /api/v0/moim/post
    public final static String PATH_MOIM_POST_CREATE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + API_MOIM_POST;
    // PATCH - /api/v0/moim/post
    public final static String PATH_MOIM_POST_UPDATE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + API_MOIM_POST;
    // DELETE - /api/v0/moim/{moimId}/post/{moimPostId}
    public final static String PATH_MOIM_POST_DELETE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/{moimId}" + API_MOIM_POST + "/{moimPostId}";


    // MOIM POST COMMENT 관련 API
    // POST - /api/v0/moim/post/comment
    public final static String PATH_POST_COMMENT_CREATE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + API_MOIM_POST + API_POST_COMMENT;
    // PATCH - /api/v0/moim/post/comment
    public final static String PATH_POST_COMMENT_UPDATE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + API_MOIM_POST + API_POST_COMMENT;
    // DELETE - /api/v0/moim/{moimId}/post/{postId}/comment/{commentId}
    public final static String PATH_POST_COMMENT_DELETE = API_SERVER + API_MOIM_VER + API_DOMAIN_MOIM + "/{moimId}" + API_MOIM_POST + "/{moimPostId}" + API_POST_COMMENT + "/{postCommentId}";


    // MEMBER POLICY 관련 API
    // PATCH - /api/v0/member/policy
    public final static String PATH_MEMBER_POLICY_UPDATE = API_SERVER + API_MEMBER_VER + API_DOMAIN_MEMBER + API_MEMBER_POLICY;


    // MEMBER 관련 API
    // POST - /api/v0/member/logout
    public final static String PATH_MEMBER_LOGOUT = API_SERVER + API_MEMBER_VER + API_DOMAIN_MEMBER + "/logout";
    public final static String PATH_MEMBER_DELETE = API_SERVER + API_MEMBER_VER + API_DOMAIN_MEMBER + "/delete";
    public final static String PATH_MEMBER_GET_VIEW = API_SERVER + API_MEMBER_VER + API_DOMAIN_MEMBER + "/{memberId}";
    public final static String PATH_MEMBER_CONFIRM_PW = API_SERVER + API_MEMBER_VER + API_DOMAIN_MEMBER + "/pw";
    public final static String PATH_MEMBER_GET_DETAIL_VIEW = API_SERVER + API_MEMBER_VER + API_DOMAIN_MEMBER + "/detail" + "/{memberId}";
    public final static String PATH_MEMBER_CHANGE_NICKNAME = API_SERVER + API_MEMBER_VER + API_DOMAIN_MEMBER + "/nickname";
    public final static String PATH_MEMBER_CHANGE_PASSWORD = API_SERVER + API_MEMBER_VER + API_DOMAIN_MEMBER + "/pw";



    // 이하로는 점검되지 않은 URL
    public final static String API_MOIM_REVIEW = "/review";
    public final static String API_MOIM_RULES = "/rules";
    public final static String API_MOIM_SESSION = "/session";



    public final static String API_SMS_VER = "/v0";
    public final static String API_SMS = "/sms";
    public final static String API_NOTI_VER = "/v0";
    public final static String API_NOTI = "/notification";
    public final static String API_MOIM_SCHEDULE = "/schedule";


    // {} 안에 있는 변수를 대상 String 으로 치환해준다
    public static String setParameter(String target, String [] before, String [] after){

        if (before.length != after.length) {
            throw new RuntimeException("Parameter 치환 갯수가 다릅니다");
       }

        for (int i = 0; i < before.length; i++) {
            String withBrackets = "{" + before[i] + "}";
            target = target.replace(withBrackets, after[i]);
        }

        return target;
    }
}
