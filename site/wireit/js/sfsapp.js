  SFSApp = {} || window.SFSApp;
  var editor_count=0;
  var editors={};
  $(document).ready(function(){
    SFSApp.msg_bar=$('.js-message-bar');
     var $tmp_form = $('#create_proc_form');
     $.fn.serializeObject = function(){
          var o = {};
          var a = this.serializeArray();
          $.each(a, function() {
              if (o[this.name]) {
                  if (!o[this.name].push) {
                      o[this.name] = [o[this.name]];
                  }
                  o[this.name].push(this.value || '');
              } else {
                  o[this.name] = this.value || '';
              }
          });
          return o;
        };
     $('#create_proc_form button').click(function(e){
       console.log("haha you clicked me");
     });


     $('#create_proc_form').submit(function(e){
        e.preventDefault();
        e.stopPropagation();
        var editor = ace.edit($(this).find('.js-editor')[0]);
        

              /*$.post('./helpers/create_process.php', $tmp_form.serialize(), function(data){
        });*/
        console.log($('#create_proc_form').serializeObject());
        var data=$('#create_proc_form').serializeObject();
        var script = editor.getValue();
        script=script.replace(/[\n\r\t]/g, "");
        data['func']=script;
        $.post('./helpers/create_process.php', data, function(data){
          console.log("response");
          console.log(data);
          SFSApp.msg_bar.append(data);
        });
        return false;
    });
    console.log($('#create_proc_form'));
     $('#btn-load-proc').click(function(e){
       e.stopPropagation();
       e.preventDefault();
       $.post('./helpers/update_process.php', $('#update_proc_form').serialize(), function(data){
         //console.log(data);
         var json=$.parseJSON(data);
         //alert(json['properties']['script']['func']);
         //var $div=$('<div></div>');
         //$div.html(JSON.stringify(json, null, 4));
         //$div.appendTo($('body'));
         //alert(data);
         var script=js_beautify(json['properties']['script']['func']);
         /*$('<div id="editor'+editor_count+'" class="js-editor ui-editor"><div>').text(JSON.stringify(json,null,4)).appendTo($('#dynamic_update_proc_area')).wrap('<div class="ui-editor-wrapper"></div>');
        var editor=editors[editor_count]=ace.edit("editor"+editor_count);
        editor.setTheme("ace/theme/monokai");
        editor.getSession().setMode("ace/mode/json");
        console.log(editor.getValue());
        editor_count++;*/
          $("#update_proc_form").find('.ui-editor-wrapper').remove();
         $('<div id="editor'+editor_count+'" class="js-editor ui-editor"><div>').text(script).appendTo($('#dynamic_update_proc_area')).wrap('<div class="ui-editor-wrapper"></div>');
        var editor=editors[editor_count]=ace.edit("editor"+editor_count);
        editor.setTheme("ace/theme/monokai");
        editor.getSession().setMode("ace/mode/javascript");
        console.log(editor.getValue());
        //alert(editor.getValue());
        editor_count++;
        //console.log(json);
        //console.log(typeof(data));
       },"json");
       return false;
     });
     $('#update_proc_form').submit(function(e){
       e.preventDefault();
       e.stopPropagation();
       console.log("time to update");
     });
  });
