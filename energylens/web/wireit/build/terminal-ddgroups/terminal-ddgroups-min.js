YUI.add("terminal-ddgroups",function(b,a){b.TerminalDDGroups=function(c){b.after(this._renderUIgroups,this,"renderUI");};b.TerminalDDGroups.ATTRS={groups:{value:["terminal"]},showGroups:{value:true}};b.TerminalDDGroups.prototype={_renderUIgroups:function(){if(this.get("editable")){this._renderTooltip();}},_renderTooltip:function(){if(this.get("showGroups")){var c=new b.Overlay({render:this.get("boundingBox"),bodyContent:this.get("groups").join(",")});c.set("align",{node:this.get("contentBox"),points:[b.WidgetPositionAlign.TC,b.WidgetPositionAlign.BC]});c.get("contentBox").addClass(this.getClassName("dd-groups"));}}};},"@VERSION@",{"requires":["terminal-dragedit"]});