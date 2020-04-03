function insert(tag, id) {
	var area = document.getElementById(id);
	var start = area.selectionStart;
	var end = area.selectionEnd;
	var val = area.value;

	if(tag.includes("=")) {
		var endtag = tag.substring(0, tag.indexOf("="));
	} else {
		var endtag = tag;
	}

	area.value = val.substring(0, start) + "[" + tag + "]" + val.substring(start, end) + "[/" + endtag + "]" + val.substring(end, val.length);
}