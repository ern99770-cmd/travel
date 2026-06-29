import request, { post, get } from "@/utils/request";
// 登陆
export const login = (params) => post("/login",params)
//登出
export const logout = () => get("/login/logout")
//获取登陆用户信息
export const getUser = () => get("/user/getUserInfo")
//修改个人信息
export const setUserInfo = (params) => post("/user/setUserInfo",params)
//修改个人头像
export const setUserAvatar = (params) => get("/user/setUserAvatar",params)

export const changePassword = (params) => post("/user/changePassword",params)
//保存用户
export const saveUser = (params) => post("/user/saveUser",params)
//获取邮箱验证码
export const getEmailReg = (params) => get("/user/getEmailReg",params)
//忘记密码
export const forgetPassword = (params) => post("/user/forgetPassword",params)
//获取轮播图
export const getSysRotationsList = (params) => get("/rotations/getSysRotationsList",params)
//获取首页景点
export const getSysAttractionsIndex = (params) => get("/attractions/getSysAttractionsIndex",params)
//获取热门景点(按访问量排序)
export const getSysAttractionsHot = (params) => get("/attractions/getSysAttractionsHot",params)
//获取推荐景点(根据用户喜好)
export const getSysAttractionsRecommend = (params) => get("/attractions/getSysAttractionsRecommend",params)
//获取用户数量
export const getUserCount = (params) => get("/user/getUserCount",params)
//获取首页路线
export const getSysLineIndex = (params) => get("/line/getSysLineIndex",params)
//查询景点
export const getSysAttractionsPage = (params) => post("/attractions/getSysAttractionsPage",params)
//根据id查询景点
export const getSysAttractionsById = (params) => get("/attractions/getSysAttractionsById",params)
//查询旅游线路
export const getSysLinePage = (params) => post("/line/getSysLinePage",params)
//根据id查询旅游线路
export const getSysLineById = (params) => get("/line/getSysLineById",params)
//增加路线点击量/热度
export const addLineHits = (params) => get("/line/addLineHits",params)
//查询酒店
export const getSysHotelPage = (params) => post("/hotel/getSysHotelPage",params)
//根据id查询酒店
export const getSysHotelById = (params) => get("/hotel/getSysHotelById",params)
//后去列表
export const getSysAttractionsList = (params) => get("/attractions/getSysAttractionsList",params)
//查询资讯
export const getSysForumPage = (params) => post("/forum/getSysForumPage",params)
//根据id查询资讯
export const getSysForumById = (params) => get("/forum/getSysForumById",params)
//增加资讯阅读量
export const addForumViews = (params) => get("/forum/addForumViews",params)
//查询收藏
export const getSysFavorPage = (params) => post("/favor/getSysFavorPage",params)
//根据id查询收藏
export const getSysFavorById = (params) => get("/favor/getSysFavorById",params)
//保存收藏
export const saveSysFavor = (params) => post("/favor/saveSysFavor",params)
//更新收藏
export const editSysFavor = (params) => post("/favor/editSysFavor",params)
//删除SysFavor
export const removeSysFavor = (params) => get("/favor/removeSysFavor",params)
//获取收藏
export const getSysFavor = (params) => get("/favor/getSysFavor",params)
//查询景点预约
export const getSysAttractionOrderPage = (params) => post("/order/getSysAttractionOrderPage",params)
//保存景点预约
export const saveSysAttractionOrder = (params) => post("/order/saveSysAttractionOrder",params)
//更新景点预约
export const editSysAttractionOrder = (params) => post("/order/editSysAttractionOrder",params)
//获取房型
export const getSysHotelItemList = (params) => get("/item/getSysHotelItemList",params)
//查询酒店预约
export const getSysHotelOrderPage = (params) => post("/order/getSysHotelOrderPage",params)
//根据id查询酒店预约
export const getSysHotelOrderById = (params) => get("/order/getSysHotelOrderById",params)
//保存酒店预约
export const saveSysHotelOrder = (params) => post("/order/saveSysHotelOrder",params)
//更新酒店预约
export const editSysHotelOrder = (params) => post("/order/editSysHotelOrder",params)
//查询评论
export const getSysCommentsPage = (params) => post("/comments/getSysCommentsPage",params)
//根据id查询评论
export const getSysCommentsById = (params) => get("/comments/getSysCommentsById",params)
//保存评论
export const saveSysComments = (params) => post("/comments/saveSysComments",params)
//更新评论
export const editSysComments = (params) => post("/comments/editSysComments",params)
//删除SysComments
export const removeSysComments = (params) => get("/comments/removeSysComments",params)
//根据目标id和类型查询评论
export const getCommentsByTargetId = (params) => get("/comments/getCommentsByTargetId",params)
// 获取会员信息
export const getMemberInfo = (params) => get("/member/info", params)
// 签到
export const signIn = (params) => post("/member/signin", params)
// 获取积分记录
export const getPointsLog = (params) => get("/member/points/log", params)
// 获取优惠券列表
export const getCoupons = (params) => get("/member/coupons", params)
// 兑换优惠券
export const exchangeCoupon = (params) => post("/member/coupon/exchange", params)
// 获取我的优惠券
export const getMyCoupons = (params) => get("/member/coupons/my", params)
// 获取我的可用优惠券（按金额筛选）
export const getAvailableCoupons = (params) => get("/member/coupons/available", params)
// 使用优惠券
export const useCoupon = (params) => post("/member/coupon/use", params)
// 获取商品列表
export const getProducts = (params) => get("/member/products", params)
// 兑换商品
export const exchangeProduct = (params) => post("/member/product/exchange", params)
// 获取我的兑换记录
export const getExchangeRecords = (params) => get("/member/exchange/records", params)
// 购买会员
export const purchaseMember = (params) => post("/member/purchase", params)
// 使用积分抵扣
export const deductionWithPoints = (params) => post("/member/deduction", params)

// 旅游分享相关API
export const getSysTravelSharePage = (params) => post("/share/getSysTravelSharePage", params)
export const getSysTravelShareById = (params) => get("/share/getSysTravelShareById", params)
export const saveSysTravelShare = (params) => post("/share/saveSysTravelShare", params)
export const editSysTravelShare = (params) => post("/share/editSysTravelShare", params)
export const removeSysTravelShare = (params) => get("/share/removeSysTravelShare", params)
export const likeShare = (params) => request.post("/share/likeShare", null, { params })
export const getMySharePage = (params) => post("/share/getMySharePage", params)

// AI 智能规划（讯飞接口较慢，单独延长超时）
export const aiChat = (params) => post("/ai/chat", params, { timeout: 120000 })
export const generateAiPlan = (params) => post("/ai/plan/generate", params, { timeout: 180000 })
export const getMyPlanPage = (params) => post("/ai/plan/getMyPlanPage", params)
export const getPlanById = (params) => get("/ai/plan/getById", params)
export const removePlan = (params) => get("/ai/plan/remove", params)
export const sharePlanToTravelShare = (params) => post("/ai/plan/share", null, { params })
