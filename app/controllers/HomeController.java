package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private final MessagesApi messagesApi;

    private final DatasetService datasetService;

    private final AppService appService;

    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi,
                          DatasetService datasetService, AppService appService) {
        this.webpageDatasetForm = formFactory.form(WebpageDataset.class);
        this.messagesApi = messagesApi;
        this.datasetService = datasetService;
        this.appService = appService;
    }

    public Result index(Http.Request request) {
        return ok(
                views.html.index.render(
                        datasetService.listDataSet(),
                        webpageDatasetForm,
                        request,
                        messagesApi.preferred(request)));
    }

    public Result uploadFromWebPage(Http.Request request) {
        final Form<WebpageDataset> form = webpageDatasetForm.bindFromRequest(request);

        // form出错
        if (form.hasErrors()) {
            log.error("form出错, 报错信息为: {}", form.errors());
            // return 400
            log.warn("访问出错: 400");
            return badRequest(
                    views.html.index.render(
                            datasetService.listDataSet(),
                            form,
                            request,
                            messagesApi.preferred(request)));
        }


        WebpageDataset webpageDataset = form.get();
        // token出错
        if (!webpageDataset.getToken().equals(datasetService.getToken())) {
            return redirect(routes.HomeController.index()).flashing("alert alert-warning", "token错误!");
        }


        final Http.MultipartFormData.FilePart<TemporaryFile> uploadedFile = webpageDataset.getFile();
        // 文件为空
        if (uploadedFile == null) {
            return redirect(routes.HomeController.index()).flashing("alert alert-warning", "文件为空!");
        }


        // 存储文件
        boolean saveResult = datasetService.saveDataset(uploadedFile);
        if (saveResult) {
            return redirect(routes.HomeController.index()).flashing("alert alert-success", "上传数据集成功!");
        } else {
            // 服务器内部错误, 存储失败, 需要查看日志
            return redirect(routes.HomeController.index()).flashing("alert alert-warning", "服务器错误, 存储失败!");
        }
    }

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

    public Result viewDataset(String name) {
        try {
            String urlName = URLEncoder.encode(name, "UTF-8");
            return ok().sendFile(new File(datasetService.getDatasetPath(name).toString()), true)
                    .as("text/plain; charset=utf-8")
                    .withHeader("Cache-Control", "no-cache")
                    .withHeader("Content-Disposition", String.format("inline; filename=\"%s\"", urlName));
        } catch (UnsupportedEncodingException e) {
            log.error("编码文件名出错, 文件名为: {}, 报错信息: {}", name, ExceptionUtils.getStackTrace(e));
            return redirect(routes.HomeController.index()).flashing("alert alert-warning", "服务器错误!");
        }
    }

    public Result downloadDataset(String name) {
        try {
            String urlName = URLEncoder.encode(name, "UTF-8");
            return ok().sendFile(new File(datasetService.getDatasetPath(name).toString()), false)
                    .as("application/x-download")
                    .withHeader("Cache-Control", "no-cache")
                    .withHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", urlName));
        } catch (UnsupportedEncodingException e) {
            log.error("编码文件名出错, 文件名为: {}, 报错信息: {}", name, ExceptionUtils.getStackTrace(e));
            return redirect(routes.HomeController.index()).flashing("alert alert-warning", "服务器错误!");
        }
    }

    public Result downloadApp() {
        Path appPath = appService.getAppPath();
        if (appPath == null || Files.notExists(appPath)) {
            return redirect(routes.HomeController.index()).flashing("alert alert-warning", "App文件不存在!");
        }

        try {
            String urlName = URLEncoder.encode(appService.getAppName(), "UTF-8");
            return ok().sendFile(new File(appPath.toString()), false)
                    .as("application/x-download")
                    .withHeader("Cache-Control", "no-cache")
                    .withHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", urlName));
        } catch (UnsupportedEncodingException e) {
            log.error("编码App文件名出错, 报错信息: {}", ExceptionUtils.getStackTrace(e));
            return redirect(routes.HomeController.index()).flashing("alert alert-warning", "服务器错误!");
        }
    }

    public Result test() {
        ObjectNode json = Json.newObject();
        json.put("msg", "用于测试");
        json.put("status", "访问正常");
        return ok(json);
    }
}
