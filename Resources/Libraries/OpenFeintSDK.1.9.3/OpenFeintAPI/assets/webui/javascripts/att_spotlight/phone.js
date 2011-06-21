(function($) {
    OF.spotlight.buyButtonLabel = function(price, includeBuy) {
        if (typeof price == "undefined") {
            return "Buy";
        } else if (price <= 0) {
            return "Free";
        } else {
            price = OF.spotlight.formatPrice(price);
            return includeBuy ? "Buy <div class='price'>$" + price + "</div>" : "$" + price;
        }
    };

    OF.spotlight._homeInit = function() {
        OF.page.requestGameDetail = function(game_id) {
        };
    };

    OF.spotlight._dataReady = function() {
        $("#more_great_games .special_msg").unhide();

        $("div.article").find("img").each(function() {
            var current = $(this);
            var imgSrc = current.attr("src");
            var containerWidth = current.parent().width();
            var frameNode = current.next("div.frame");
            frameNode.css({
                "background-image": "url(" + imgSrc + ")",
                "height": "100%",
                "width": containerWidth,
                "-webkit-background-size":  containerWidth + "px auto"
            });
        });
    }
})(jQuery);