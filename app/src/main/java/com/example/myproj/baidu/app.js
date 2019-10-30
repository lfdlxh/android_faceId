//app.js
//加载文件管理模块fs
const fs=require('fs');
//加载express框架
const express=require('express');
//加载http协议
const http=require('http');


//文件上传中间件(指定上传的临时文件夹是/uploads)
const multer=require('multer');
let upload = multer({ dest: 'uploads/' });
//声明中间件存储的位置在uploads,如果没有这个文件夹会自动创建
//.let的用法类似于var，但是所声明的变量，只在let命令所在的代码块内有效。var定义的变量为全局变量。

let app=express();

const FILE_PATH="public/images/";

//HttpServer服务的中间件(public目录下的index.html为首页)
app.use(express.static('public'));

//文件上传的处理（avatar是上传时的filedName）
//这里第一个参数/upload 是post 访问的页面， 比如url : http://localhost:8080/upload,single是multer的方法，用来取一个文件
//文件名叫picture，req --》request  res--》response
app.post('/upload', upload.single('picture'), function (req, res, next) {
    //req.body是普通表单域
    //req.file是文件域
    console.log(req.file);
    let msg={
        body:req.body,
        file:req.file
    }
    //将临时文件上传到/public/images中
    let output=fs.createWriteStream(FILE_PATH+req.file.originalname);
    let input=fs.createReadStream(req.file.path);
    input.pipe(output);
    res.json(msg);
})
//接收前端的请求，返回上传图片的列表
app.get("/files",function (req,res) {
    fs.readdir('public/images',function (err,dir) {
        res.json(dir);
    })
})
//启动Express服务器
let server=http.createServer(app);
server.listen(8080,function () {
    console.log("start server at port 8000");
})