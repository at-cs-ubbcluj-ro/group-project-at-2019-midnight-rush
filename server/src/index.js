const express = require('express');
const Koa = require('koa');
const Router = require('koa-router');
const fs = require('fs');
const bodyParser = require('koa-bodyparser');
const app = new Koa();
const router = new Router();

app.use(bodyParser());

router.post('/addData', ctx => {
	const value = ctx.request.body.value;
	let dataToAppend = `${Math.floor(Date.now() / 1000)} ${value} \n`;

    fs.appendFileSync("D:\\UBB\\Info\\Anul3\\Semestrul2\\AndroidThings\\demo\\data.txt", dataToAppend);
	console.log("Appended " + dataToAppend);
    ctx.body = 'added';
    ctx.status = 200;
	ctx.type = 'text/plain';
});

router.get('/getData', async (ctx) => {
  const fileData = await getGata();
  ctx.status = 200;
  ctx.body = fileData;
});

async function getGata() {
  return await getDataFromFile();
}

async function getDataFromFile() {
  return new Promise((resolve, reject) => {
	const file = "D:\\UBB\\Info\\Anul3\\Semestrul2\\AndroidThings\\demo\\data.txt";
    
	fs.readFile(file, 'utf8', function(err, data) {
		if (err) {
			reject(err);
		}

		resolve(data);
	});
   
  });
};

app.use(router.routes());

app.listen(2029);