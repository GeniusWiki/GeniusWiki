/**
 * Update: 
 * 1. all validation removed.
 * 2. formObj change to document.getElementById() from document.forms[0].
 * 3. TinyMCEPopup.close() removed
 * 4. init() method removed
 * @return
 */
function mergeCells(input) {
	var args  = [];
	args["numcols"] = input[0];
	args["numrows"] = input[1];

	tinyMCE.execCommand("mceTableMergeCells", false, args);
}

