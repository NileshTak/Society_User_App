package com.nil_projects_society_user_app

class ComplaintClass(val CompheadLine : String,val ComplaintImg : String,val CompUpdatedDate : String,val CompProcess : String)
{
    constructor() : this("","","","")
}


class SliderImgClass(val Img1 : String,val Img2: String,val Img3 : String,val Img4 : String,val Img5 : String)
{
    constructor() : this("","","","","")
}

class AddNotifiClass(val id : String,val noti : String,val imageUrl : String,val currentTime : String)
{
    constructor() : this("","","","")
}


class reportModelClass(val currentTime: String,val buildingnotice : String, val id : String,val imageUrl : String,val wing : String,
                       val userid : String)
{
    constructor() : this("","","","","","")
}

class UserSocietyClass(val UserID : String,val Profile_Pic_url : String,val UserName : String,val UserEmail : String,
                       val City: String,val SocietyName : String,val Wing : String,val AlternateMobile :String,
                       val FlatNo : String,val UserRelation : String,val userAuth : String,val MobileNumber : String)
{
    constructor() : this("","","","","","","","","","","","")
}

class FetchWorkerClass(val id : String,val name : String,val imageUrl : String,val address : String,
                       val type : String,val mobile : String,val dateofjoining : String,val speciality : String)
{
    constructor() : this("","","","","",",","","")
}




