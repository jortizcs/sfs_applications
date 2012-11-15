// graphite.js

(function ($) {
    $.fn.graphite = function (options) {
        if (options === "update") {
            $.fn.graphite.update(this);
            return this;
        }

        // Initialize plugin //
        options = options || {};
        var settings = $.extend({}, $.fn.graphite.defaults, options);

        return this.each(function () {
            $this = $(this);

            $this.data("graphOptions", settings);
            $.fn.graphite.render($this, settings);
        });

    };

    $.fn.graphite.render = function($img, options) {
        // Render a new image. //
        var src = options.url + "?";
        $.each(options, function (key, value) {
            if (key === "target") {
                $.each(value, function (index, value) {
                    src += "&target=" + value;
                });
            } else if (value !== null && key !== "url") {
                src += "&" + key + "=" + value;
            }
        });

        src = src.replace(/\?&/, "?");
        $img.attr("src", src);
        $img.attr("height", options.height);
        $img.attr("width", options.width);
    };

    $.fn.graphite.update = function($img, options) {
        options = options || {};
        var settings = $.extend({}, $img.data("graphOptions"), options);
        $.fn.graphite.render($img, settings);
    };

    // Default settings. 
    // Override with the options argument for per-case setup
    // or set $.fn.graphite.defaults.<value> for global changes
    $.fn.graphite.defaults = {
        from: "-1hour",
        height: "300",
        until: "now",
        url: "/render/",
        width: "940",
    };

}(jQuery));