JQuery version: 1.62


colorpicker, dropdown and datapicker is expected to maintain by ourselves.
Major change on colorpicker, dropdown:
Don't create duplicated div if they are invoked multiple times.

Major datepicker:
show & hide method change to display:none|block from css visibility:visible/hide

Nivo update:
CSS:
 .slider-wrapper{
 	margin-bottom:45px;
 }
.nivoSlider {
	margin: 0px auto; //add by NDP
}
JS:
* var kids=slider.children("img"); // add "img" for image only slide.
* Remove "fade" style from JS as it does not work perfectly, the rolling image does not cover to image area. 
