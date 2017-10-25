package com.geekhome.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.geekhome.common.vo.ErrorCode;
import com.geekhome.common.vo.ExecuteResult;
import com.geekhome.common.vo.MarkdownUploadImage;
import com.geekhome.common.vo.PageableResultJson;
import com.geekhome.entity.Comment;
import com.geekhome.entity.QuestionAnswers;
import com.geekhome.entity.dao.CommentDao;
import com.geekhome.entity.dao.QuestionAnswersDao;
import com.geekhome.entity.service.CommentService;
import com.geekhome.entity.service.QuestionAnswersService;

@RestController
@RequestMapping("questionAnswers")
public class QuestionAnswersController {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${upload.path}")
	String upload_dir;

	@Autowired
	QuestionAnswersService questionAnswersService;
	@Autowired
	QuestionAnswersDao questionAnswersDao;
	@Autowired
	CommentService commentService;
	@Autowired
	CommentDao commentDao;

	@RequestMapping(value = "/uploadImage")
	@CrossOrigin
	public MarkdownUploadImage uploadImage( @RequestParam("editormd-image-file") MultipartFile file)
			throws IOException {
		try {
			String fileName = file.getOriginalFilename();
			String realPath = new Date().getTime() + fileName.substring(fileName.lastIndexOf("."));
			File tempFile = new File(upload_dir + realPath);
			if (!tempFile.getParentFile().exists()) {
				tempFile.mkdirs();
			}
			FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(tempFile));
			return new MarkdownUploadImage(1, "上传成功！", realPath);
		} catch (final Exception e) {
			logger.error("", e);
			return new MarkdownUploadImage(0, "上传失敗!", null);
		}
	}
	
	@RequestMapping("saveQuestionAnswers")
	@CrossOrigin
	public ExecuteResult<Boolean> saveQuestionAnswers(@RequestBody QuestionAnswers questionAnswers) {
		final ExecuteResult<Boolean> result = new ExecuteResult<>();
		try {
			questionAnswersService.saveQuestionAnswers(questionAnswers);
			result.setSuccess(true);
		} catch (final Exception e) {
			logger.error("", e);
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.EXCEPTION.getErrorCode());
			result.setErrorMsg(ErrorCode.EXCEPTION.getErrorMsg());
		}
		return result;
	}
	
	@RequestMapping("saveComment")
	@CrossOrigin
	public ExecuteResult<Boolean> saveComment(@RequestBody Comment comment) {
		final ExecuteResult<Boolean> result = new ExecuteResult<>();
		try {
			comment.setCreateTime(new Date());
			commentDao.save(comment);
			result.setSuccess(true);
		} catch (final Exception e) {
			logger.error("", e);
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.EXCEPTION.getErrorCode());
			result.setErrorMsg(ErrorCode.EXCEPTION.getErrorMsg());
		}
		return result;
	}
	
	@RequestMapping("getQuestionAnswersById")
	@CrossOrigin
	public ExecuteResult<QuestionAnswers> getQuestionAnswersById(Long id) {
		final ExecuteResult<QuestionAnswers> result = new ExecuteResult<>();
		try {
			QuestionAnswers questionAnswers = questionAnswersService.getQuestionAnswersById(id);
			result.setData(questionAnswers);
			result.setSuccess(true);
		} catch (final Exception e) {
			logger.error("", e);
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.EXCEPTION.getErrorCode());
			result.setErrorMsg(ErrorCode.EXCEPTION.getErrorMsg());
		}
		return result;
	}
	
	@RequestMapping("addQuestionAnswersBrowseCnt")
	@CrossOrigin
	public ExecuteResult<Boolean> addQuestionAnswersBrowseCnt(Long id) {
		final ExecuteResult<Boolean> result = new ExecuteResult<>();
		try {
			QuestionAnswers answers = questionAnswersDao.findOne(id);
			answers.setBrowseCount(answers.getBrowseCount()+1);
			questionAnswersDao.save(answers);
			result.setSuccess(true);
		} catch (final Exception e) {
			logger.error("", e);
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.EXCEPTION.getErrorCode());
			result.setErrorMsg(ErrorCode.EXCEPTION.getErrorMsg());
		}
		return result;
	}

	@RequestMapping("questionAnswersList")
	@CrossOrigin
	public PageableResultJson questionAnswersList(@RequestParam(value = "page") Integer page, Long labelId) {
		PageableResultJson tableJson = new PageableResultJson();
		Page<QuestionAnswers> pageData = questionAnswersService.findQuestionAnswersByLabelIdList(labelId, page,10);
		tableJson.setData(pageData.getContent());
		tableJson.setPageSize(10);
		tableJson.setTotalPageNumber(pageData.getTotalPages());
		return tableJson;
	}
	
	@RequestMapping("commentList")
	@CrossOrigin
	public PageableResultJson commentList(@RequestParam(value = "page") Integer page, Long id) {
		PageableResultJson tableJson = new PageableResultJson();
		Page<Comment> pageData = commentService.findCommentByLabelIdList(id, page,10);
		tableJson.setData(pageData.getContent());
		tableJson.setPageSize(10);
		tableJson.setTotalPageNumber(pageData.getTotalPages());
		return tableJson;
	}

}
