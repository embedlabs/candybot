(function($) {
    var clientWidth = window.outerWidth;
    OF.spotlight.pad =  clientWidth >= 800;
    OF.spotlight.truncate = function(string, maxLength) {
        if (string.length > maxLength) {
            string = string.substring(0, maxLength) + '...';
            return string;
        } else {
            return string;
        }
    };
    OF.spotlight.formatPrice = function(price) {
        var newPriceStr = price + "";
        var dotIndex = newPriceStr.indexOf(".");
        if (dotIndex < 0) {
            newPriceStr = newPriceStr + ".00";
        } else if (newPriceStr.substring(dotIndex + 1).length == 1) {
            newPriceStr = newPriceStr + "0";
        }
        return newPriceStr;
    }


    OF.spotlight.homeInit = function() {
        var featuredGameUrl;
        if (OF.spotlight.pad) {
            featuredGameUrl = "/xp/promotions/att_tablet_featured/games"
        } else {
            featuredGameUrl = "/xp/promotions/att_featured/games"
        }
        OF.page.package_identifier = null;
        $('#show_dashboard').unhide();
        $('#show_dashboard .cell').touch(function() {
            OF.action('dashboard');
        });

        OF.spotlight._homeInit();
        OF.page.renderFeaturedGame = function(game) {
            /*
                because homeInit will be called when back to spotlight home page from feint page in phone version,
                so remove featured_game node firstly to prevent duplicated featured game
             */
            $("#featured > #featured_game").remove();
            $("#featured").append(tmpl("featured_game", {
                button: "purchase_button",
                promoted_game: game.promoted_game
            }));
            $('#featured .game').addClass('active');
            $("#featured").unhide();
        };
        OF.page.renderGamesList = function(destination, promoted_games, button) {
            var html = $.map(promoted_games, function(data) {
                return tmpl("game", {button:button, promoted_game:data.promoted_game})
            });
            $(destination).html(html.join(" "))
                .children("div.article:last").addClass("last")
        };
        OF.api(featuredGameUrl, {
            params: {platform: "android"},
            success: function(data) {
                var featuredGame = data.promoted_games[0];
                var moreGames = data.promoted_games.slice(1, 6);

                OF.page.renderFeaturedGame(featuredGame);
                if (OF.page.params.game_id) {
                    game_id = OF.page.params.game_id;
                } else {
                    game_id = featuredGame.promoted_game.game.id;
                }
                OF.page.requestGameDetail(game_id);

                if (data.promoted_games.length == 0) {
                    return;
                }
                OF.page.renderGamesList("#more_great_games .games", moreGames, "purchase_button");
                $("#more_great_games").unhide();
                $("#content").unhide();
                $("#main_loader").hide();

                OF.spotlight._dataReady();
            }
        });
    };
})(jQuery);