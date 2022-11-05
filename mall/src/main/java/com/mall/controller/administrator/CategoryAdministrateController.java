package com.mall.controller.administrator;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.ICategoryService;
import com.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;
@Controller
@RequestMapping("/administrate/category/")
public class CategoryAdministrateController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;
    //添加分类
    @RequestMapping(value = "add_category.do")//接口定义：login.do，登录指定是post请求
    @ResponseBody//在返回时自动通过SpringMVC的jackson插件将我们的返回值序列化成Jackson
    public ServerResponse addCategory(HttpSession session, @RequestParam(value = "parentId",defaultValue = "0") int parentId, String categoryName ){
        //判断是否登录
        User user=(User) session.getAttribute (Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (),"用户未登录，请登录");
        }
        //校验是否为管理员
        if(iUserService.checkAdminRole (user).isSuccess ()){
            //增加分类
            return iCategoryService.addCategory (categoryName,parentId);
        }else{
            return ServerResponse.createByErrorMessage ("不是管理员，没有权限!");
        }

    }

    //更新分类名字
    @RequestMapping("reset_category_name.do")
    @ResponseBody
    //更新品类名称
    public ServerResponse resetCategoryName(HttpSession session,Integer categoryId,String categoryName){
        //校验是否登录
        User user=(User) session.getAttribute (Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (),"用户未登录，请登录");
        }
        //校验是否为管理员
        if(iUserService.checkAdminRole (user).isSuccess ()){
            //更新品类名称
            return iCategoryService.updateCategoryName (categoryId,categoryName);
        }else{
            return ServerResponse.createByErrorMessage ("不是管理员，没有权限！");
        }
    }
    //
    @RequestMapping("get_category.do")
    @ResponseBody
    //获取子节点（平级）
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0")Integer categoryId,String categoryName){
        //校验是否登录
        User user=(User) session.getAttribute (Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (),"用户未登录，请登录");
        }
        //校验是否为管理员
        if(iUserService.checkAdminRole (user).isSuccess ()){
            //获取
            return iCategoryService.getChildrenParallelCategory (categoryId);
        }else{
            return ServerResponse.createByErrorMessage ("不是管理员，没有权限！");
        }
    }

    @RequestMapping("get_deep_category.do")
    @ResponseBody
    //获取子节点（递归）------深度查询
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0")Integer categoryId,String categoryName){
        //校验是否登录
        User user=(User) session.getAttribute (Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (),"用户未登录，请登录");
        }
        //校验是否为管理员
        if(iUserService.checkAdminRole (user).isSuccess ()){
            //查询当前节点及子节点
            return iCategoryService.selectCategoryAndChildrenById (categoryId);
        }else{
            return ServerResponse.createByErrorMessage ("不是管理员，没有权限！");
        }
    }
}
