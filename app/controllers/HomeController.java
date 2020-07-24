package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dto.Condition;
import dto.UploadResponse;
import dto.WebpageDataset;
import lombok.extern.slf4j.Slf4j;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.Files.TemporaryFile;
import play.libs.Json;
import play.libs.exception.ExceptionUtils;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.AppService;
import services.DatasetService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Singleton
public class HomeController extends Controller {

    /* Form objects are immutable. Calls to methods like bind() and fill()
     *  will return a new object filled with the new data. */
    private final Form<WebpageDataset> webpageDatasetForm;
    private final Form<Condition> conditionForm;

    private final MessagesApi messagesApi;

    private final DatasetService datasetService;
    private final AppService appService;

    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi,
                          DatasetService datasetService, AppService appService) {
        this.webpageDatasetForm = formFactory.form(WebpageDataset.class);
        this.conditionForm = formFactory.form(Condition.class);
        this.messagesApi = messagesApi;
        this.datasetService = datasetService;
        this.appService = appService;
    }

    /**
     * 根据指定属性和升降序列出所有数据文件并在页面显示
     *
     * @param sort      作为排序依据的属性
     * @param ascending 升序还是降序
     * @return 渲染的页面
     */
    public Result index(String sort, boolean ascending) {
        return ok(views.html.index.render(sort, String.valueOf(ascending),
                datasetService.listSortedDataSet(sort, ascending),
                webpageDatasetForm, conditionForm, request(), messagesApi.preferred(request())));
    }

    /**
     * 按选择的属性和升降序重新排序列出数据文件
     *
     * @param request 包含 form 数据的请求
     * @return 渲染的页面
     */
    public Result sort(Http.Request request) {
        final Form<Condition> condForm = conditionForm.bindFromRequest(request);

        if (condForm.hasErrors()) {
            log.error("conditionForm出错, 报错信息: {}", condForm.errors());
            return badRequest(views.html.index.render("uploadDate", String.valueOf(false),
                    datasetService.listSortedDataSet("uploadDate", false),
                    webpageDatasetForm, condForm, request, messagesApi.preferred(request)));
        }

        Condition condition = condForm.get();
        String sort = condition.getSort();
        boolean asc = condition.isAscending();
        return redirect(routes.HomeController.index(sort, asc));
    }

    /**
     * 从网页上传一个数据文件
     *
     * @param request 包含 form 数据的请求
     * @return 渲染的页面，并根据是否上传成功显示对应的结果
     */
    public Result uploadFromWebPage(Http.Request request) {
        final Form<WebpageDataset> uldForm = webpageDatasetForm.bindFromRequest(request);

        if (uldForm.hasErrors()) {
            log.error("webpageDatasetForm出错, 报错信息: {}", uldForm.errors());
            return badRequest(views.html.index.render("uploadDate", String.valueOf(false),
                    datasetService.listSortedDataSet("uploadDate", false),
                    uldForm, conditionForm, request, messagesApi.preferred(request)));
        }

        WebpageDataset webpageDataset = uldForm.get();
        String sort = webpageDataset.getSort();
        boolean asc = webpageDataset.isAscending();

        if (!webpageDataset.getToken().equals(datasetService.getToken())) {
            return redirect(routes.HomeController.index(sort, asc)).flashing("alert alert-warning", "token错误!");
        }

        final Http.MultipartFormData.FilePart<TemporaryFile> uploadedFile = webpageDataset.getFile();
        if (uploadedFile == null) {
            return redirect(routes.HomeController.index(sort, asc)).flashing("alert alert-warning", "文件为空!");
        }

        boolean saveResult = datasetService.saveDataset(uploadedFile);
        if (saveResult) {
            return redirect(routes.HomeController.index(sort, asc)).flashing("alert alert-success", "上传数据集成功!");
        } else {
            // 服务器内部错误, 存储失败, 需要查看日志
            return redirect(routes.HomeController.index(sort, asc)).flashing("alert alert-warning", "服务器错误, 存储失败!");
        }
    }

    /**
     * 手机端上传数据的 API
     *
     * @param request 包含数据文件的请求
     * @return Json格式的结果
     */
    public Result fileUploadApi(Http.Request request) {
        final Http.MultipartFormData<TemporaryFile> body = request.body().asMultipartFormData();
        final Http.MultipartFormData.FilePart<TemporaryFile> uploadedFile = body.getFile("file");
        if (uploadedFile == null) {
            return badRequest(Json.toJson(
                    new UploadResponse(false, String.valueOf(BAD_REQUEST), "file is null")));
        }
        boolean saveResult = datasetService.saveDataset(uploadedFile);
        if (saveResult) {
            return ok(Json.toJson(
                    new UploadResponse(true, String.valueOf(OK),
                            "successfully uploaded " + uploadedFile.getFilename())));
        } else {
            return internalServerError(Json.toJson(
                    new UploadResponse(false, String.valueOf(INTERNAL_SERVER_ERROR),
                            "failed because of internal server error")));
        }
    }

    /**
     * 在线打开数据文件或者下载
     *
     * @param name      数据文件的全名
     * @param inline    true 则在线预览，false 则下载文件
     * @param sort      当前的排序属性
     * @param ascending 当前的升降序
     * @return inline 为 true 时在新页面预览，inline 为 false 时下载文件，出错时重新渲染页面并显示错误信息
     */
    public Result viewDataset(String name, boolean inline, String sort, boolean ascending) {
        try {
            String urlName = URLEncoder.encode(name, "UTF-8");
            String contentType = inline ? "text/plain; charset=utf-8" : "application/x-download";
            String contentDisposition = inline ? String.format("inline; filename=\"%s\"", urlName)
                    : String.format("attachment; filename=\"%s\"", urlName);
            return ok().sendFile(new File(datasetService.getDatasetPath(name).toString()), inline)
                    .as(contentType)
                    .withHeader("Cache-Control", "no-cache")
                    .withHeader("Content-Disposition", contentDisposition);
        } catch (UnsupportedEncodingException e) {
            log.error("编码文件名出错, 文件名为: {}, 报错信息: {}", name, ExceptionUtils.getStackTrace(e));
            return redirect(routes.HomeController.index(sort, ascending)).flashing("alert alert-warning", "服务器错误!");
        }
    }

    /**
     * 下载 Android APK 文件
     *
     * @param sort      当前的排序属性
     * @param ascending 当前的升降序
     * @return 能进行下载则开始下载，否则重新渲染页面并显示错误信息
     */
    public Result downloadApp(String sort, boolean ascending) {
        Path appPath = appService.getAppPath();
        if (appPath == null || Files.notExists(appPath)) {
            return redirect(routes.HomeController.index(sort, ascending))
                    .flashing("alert alert-warning", "App文件不存在!");
        }

        try {
            String urlName = URLEncoder.encode(appService.getAppName(), "UTF-8");
            return ok().sendFile(new File(appPath.toString()), false)
                    .as("application/x-download")
                    .withHeader("Cache-Control", "no-cache")
                    .withHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", urlName));
        } catch (UnsupportedEncodingException e) {
            log.error("编码App文件名出错, 报错信息: {}", ExceptionUtils.getStackTrace(e));
            return redirect(routes.HomeController.index(sort, ascending))
                    .flashing("alert alert-warning", "服务器错误!");
        }
    }
}
