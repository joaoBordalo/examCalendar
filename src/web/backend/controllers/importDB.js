//add students/teachers/rooms (all of them are .xlxs or .xls)
// POST: add

var database = require('./database');
var http = require('http');

module.exports = {
	setTimespan: function (res, calendarID, startingDate, normalSeasonDuration, appealSeasonDuration) {
		database.setTimespan(calendarID, startingDate, normalSeasonDuration, appealSeasonDuration);
		res.end();
	},
  import: function (res, userID, req) {
		var content_type = req.headers['content-type'];
		var boundary = content_type.split('; ')[1].split('=')[1];
		var userIDString = '--'
				+ boundary
				+ '\r\n'
				+ 'Content-Disposition: form-data; name="userid"'
				+ '\r\n'
				+ '\r\n'
				+ '1'
				+ '\r\n';
				
		req.headers['content-length'] = '' + (parseInt(req.headers['content-length'], 10) + userIDString.length);
		var options = {
			hostname: 'localhost',
			port: 8081,
			path: '/parser',
			method: 'POST',
			headers: req.headers
		};
		
		var req2 = http.request(options, function(res2){
			res2.pipe(res, {
			  end: true
			});
			res.statusCode = res2.statusCode;
		});
		
		var body = [];
		req.on('data', function(chunk) {
			body.push(chunk);
		}).on('end', function() {
			body = Buffer.concat(body);
			body = Buffer.concat([new Buffer(userIDString), body]);
			req2.write(body);
			req2.end();
		});
  },
  getTopics: function (res, calendarID) {
	database.getTopics(calendarID, function(err, rows, fields) {
		if (err) throw err;
		res.json(rows);
	});
  },
  setTopics: function (res, calendarID, topics) {
	database.setTopics(calendarID, topics);
	res.end();
  }
};