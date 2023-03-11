package com.lizhou.service;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lizhou.bean.Page;
import com.lizhou.bean.Student;
import com.lizhou.bean.User;
import com.lizhou.dao.impl.StudentDaoImpl;
import com.lizhou.dao.inter.StudentDaoInter;
import com.lizhou.tools.MysqlTool;
import com.lizhou.tools.StringTool;

import net.sf.json.JSONObject;

/**
 * 学生信息服务层
 * @author bojiangzhou
 *
 */
public class StudentService {
	
	private StudentDaoInter dao;
	
	public StudentService(){
		dao = new StudentDaoImpl();
	}
	

	
	/**
	 * 删除学生
	 * @param ids 学生id集合
	 * @param numbers 学生学号集合
	 * @throws Exception 
	 */
	public void deleteStudent(String[] ids, String[] numbers) throws Exception{
		//获取占位符
		String mark = StringTool.getMark(numbers.length);
		Integer sid[] = new Integer[ids.length];
		for(int i =0 ;i < ids.length;i++){
			sid[i] = Integer.parseInt(ids[i]);
		}
		
		//获取连接
		Connection conn = MysqlTool.getConnection();
		//开启事务
		MysqlTool.startTransaction();
		try {
			//删除成绩表
			dao.deleteTransaction(conn, "DELETE FROM escore WHERE studentid IN("+mark+")", sid);
			//删除学生
			dao.deleteTransaction(conn, "DELETE FROM student WHERE id IN("+mark+")", sid);
			//删除系统用户
			dao.deleteTransaction(conn, "DELETE FROM user WHERE account IN("+mark+")",  numbers);
			
			//提交事务
			MysqlTool.commit();
		} catch (Exception e) {
			//回滚事务
			MysqlTool.rollback();
			e.printStackTrace();
			throw e;
		} finally {
			MysqlTool.closeConnection();
		}
		
	}
	
	
	/**
	 * 分页获取学生
	 * @param student 学生信息
	 * @param page 分页
	 * @return
	 */
	public String getStudentList(Student student, Page page){
		//sql语句
		StringBuffer sb = new StringBuffer("SELECT * FROM student ");
		//参数
		List<Object> param = new LinkedList<>();
		//判断条件
		if(student != null){ 
			if(student.getGrade() != null){//条件：年级
				int gradeid = student.getGrade().getId();
				param.add(gradeid);
				sb.append("AND gradeid=? ");
			}
			if(student.getClazz() != null){
				int clazzid = student.getClazz().getId();
				param.add(clazzid);
				sb.append("AND clazzid=? ");
			}
		}
		//添加排序
		sb.append("ORDER BY id DESC ");
		//分页
		if(page != null){
			param.add(page.getStart());
			param.add(page.getSize());
			sb.append("limit ?,?");
		}
		String sql = sb.toString().replaceFirst("AND", "WHERE");
		//获取数据
		List<Student> list = dao.getStudentList(sql, param);
		//获取总记录数
		long total = getCount(student);
		//定义Map
		Map<String, Object> jsonMap = new HashMap<String, Object>();  
		//total键 存放总记录数，必须的
        jsonMap.put("total", total);
        //rows键 存放每页记录 list 
        jsonMap.put("rows", list); 
        //格式化Map,以json格式返回数据
        String result = JSONObject.fromObject(jsonMap).toString();
        //返回
		return result;
	}
	
	/**
	 * 获取记录数
	 * @param student
	 * @param page
	 * @return
	 */
	private long getCount(Student student){
		//sql语句
		StringBuffer sb = new StringBuffer("SELECT COUNT(*) FROM student ");
		//参数
		List<Object> param = new LinkedList<>();
		//判断条件
		if(student != null){ 
			if(student.getGrade() != null){//条件：年级
				int gradeid = student.getGrade().getId();
				param.add(gradeid);
				sb.append("AND gradeid=? ");
			}
			if(student.getClazz() != null){
				int clazzid = student.getClazz().getId();
				param.add(clazzid);
				sb.append("AND clazzid=? ");
			}
		}
		String sql = sb.toString().replaceFirst("AND", "WHERE");
		
		long count = dao.count(sql, param).intValue();
		
		return count;
	}

	/**
	 * 获取跟学生一个班的班级同学
	 * @param account
	 * @param page
	 * @return
	 */
	public String getStudentList(String account, Page page) {
		
		Student student = (Student) dao.getObject(Student.class, "SELECT * FROM student WHERE number=?", new Object[]{account});
		
		return getStudentList(student, page);
	}

	/**
	 * 获取学生详细信息
	 * @param account
	 * @return
	 */
	public Student getStudent(String account) {
		
		Student student = dao.getStudentList("SELECT * FROM student WHERE number="+account, null).get(0);
		
		return student;
	}
	
	/**
	 * 设置照片
	 * @param number
	 * @param fileName 
	 */
	public void setPhoto(String number, String fileName) {
		String photo = "photo/"+fileName;
		dao.update("UPDATE student SET photo=? WHERE number=?", new Object[]{photo, number});
	}
	
}
