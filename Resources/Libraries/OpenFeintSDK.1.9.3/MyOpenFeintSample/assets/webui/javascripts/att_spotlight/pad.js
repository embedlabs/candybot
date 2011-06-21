(function($) {
    $.fn.swipeIt = function() {
        var el = this;
        var s = {
            currentSlideIndex : 0,
            currentSlideOffset : 0,
            direction : null,
            dx : null,
            eventTypeStart : null,
            eventTypeMove : null,
            eventTypeEnd : null,
            i : null,
            lastSlideIndex : null,
            moveX : null,
            positionX : null,
            slideWidth : null,
            touchStartX : null,
            transition : null,
            startSwipeTime : null,
            endSwipeEndTime : null,
            swipeDuration : null,
            swipeSpeed : null,
            indicator : false
        };

        s.eventTypeStart = (OF.isDevice) ? 'touchstart' : 'mousedown';
        s.eventTypeMove = (OF.isDevice) ? 'touchmove' : 'mousemove';
        s.eventTypeEnd = (OF.isDevice) ? 'touchend' : 'mouseup';

        //STARTING EVENT
        $(el).live(s.eventTypeStart, function() {
            var sd = new Date();
            s.startSwipeTime = sd.getTime();
            swipeEvent(this);
        });

        // var buildIndicator = function() {
        if (s.indicator == true) {
            $(el).children().each(function(index) {
                var elClass = (s.currentSlideIndex == index) ? elClass = 'current' : elClass = '';
                $('.slide_indicator').append('<li class=' + elClass + '></li>');
            });
        }
        // };
        //
        // buildIndicator();

        var indicateCurrentIndicator = function() {
            if (s.indicator == true) {
                $(".slide_indicator").children().removeClass('current');
                $(".slide_indicator li:eq(" + s.currentSlideIndex + ")").addClass('current');
            }
        };

        var swipeEvent = function(el) {
            s.currentSlideOffset = s.currentSlideIndex * -(s.slideWidth);
            s.slideWidth = $(el).children().outerWidth(true);
            s.lastSlideIndex = $(el).children().size() - 1;
            s.touchStartX = (OF.isDevice) ? event.touches[0].pageX : event.pageX;

            var cancelTouch = function() {
                if (OF.isDevice) {
                    $(el).unbind('touchmove');
                    $(el).unbind('touchend');
                } else {
                    $(el).unbind('mousemove');
                    $(el).unbind('mouseup');
                }
            };

            //MOVING EVENT
            $(el).bind(s.eventTypeMove, function() { //mousemove or touchmove
                event.preventDefault();
                s.moveX = (OF.isDevice) ? event.touches[0].pageX : event.pageX;
                s.dx = s.moveX - s.touchStartX;
                s.transition = '-webkit-transform 0s ease-in-out';
                s.positionX = (s.currentSlideOffset + s.dx);
                $(el).css({'-webkit-transition': s.transition ,'-webkit-transform' : 'translate3d(' + s.positionX + 'px, 0 ,0)'});
            });

            //ENDING EVENT
            $(el).bind(s.eventTypeEnd, function() {
                var ed = new Date();
                s.endSwipeTime = ed.getTime();
                s.swipeDuration = s.endSwipeTime - s.startSwipeTime;
                s.swipeSpeed = (s.swipeDuration).toFixed(2);
                s.swipeSpeed = (s.swipeSpeed > 0.25) ? 0.25 : (s.swipeDuration * 0.002).toFixed(2);

                if (s.direction == null) {
                    s.direction = s.dx;
                    event.preventDefault();
                }
                if (Math.abs(s.dx) > 1) {
                    s.direction = s.dx > 0 ? 'right' : 'left';
                }
                if (s.direction == 'left') {
                    if (s.currentSlideIndex == s.lastSlideIndex) {
                        s.transition = '-webkit-transform 0s ease-in-out';
                        s.positionX = s.lastSlideIndex * s.slideWidth;
                        s.dx = null;
                    } else {
                        s.i = 1;
                    }
                }
                if (s.direction == 'right') {
                    if (s.currentSlideIndex == 0) {
                        s.transition = '-webkit-transform 0s ease-in-out';
                        s.positionX = 0;
                        s.dx = null;
                    } else {
                        s.i = -1;
                    }
                }
                if (Math.abs(s.dx) > Math.abs(s.slideWidth * .25)) {
                    s.transition = '-webkit-transform ' + s.swipeSpeed + 's ' + 'ease-in-out';
                    s.positionX = (s.currentSlideIndex + s.i) * s.slideWidth;
                    s.currentSlideIndex = s.currentSlideIndex + s.i;
                } else {
                    s.transition = '-webkit-transform ' + s.swipeSpeed + 's ' + 'ease-in-out';
                    s.positionX = s.currentSlideIndex * s.slideWidth;
                }
                $(el).css({'-webkit-transition': s.transition ,'-webkit-transform' : 'translate3d(' + -(s.positionX) + 'px, 0 ,0)'});
                //      indicateCurrentIndicator();
                cancelTouch();
            });
        }
    };
    OF.spotlight.buyButtonLabel = function(price, includeBuy) {
        if (typeof price == "undefined") {
            return "Buy";
        } else if (price <= 0) {
            return "Free";
        } else {
            price = OF.spotlight.formatPrice(price);
            return includeBuy ? "<div class='price'>$" + price + "</div> Buy" : "$" + price;
        }
    };

    OF.spotlight._homeInit = function() {
        // gets called when not all information could be loaded
        OF.page.abort = function() {
            OF.alert('No Game Info', 'The developer of this game has not yet setup all the information for this screen.');
        };
        OF.page.installCallback = function(installed) {
            if (installed) {
                $('#buy').addClass('disabled').text('Installed').unhide();
            } else {
                $('#buy').unhide().touch(function() {
                    OF.action('openMarket', { package_name: OF.page.package_identifier });
                });
            }
        };
        OF.page.renderThumbnails = function(screenshots) {
            // create elements for each slide
            $('#slides')
                    .css({'-webkit-transform': 'translate3d(0px, 0px, 0px)'})
                    .html(tmpl('slides_tmpl', { count: screenshots.length }))
                    .swipeIt();
            // loop thorugh each image to load and bind the loading handler
            $.each(screenshots.slice(0, 4), function(index) {
                var imgUrl = this.url;
                var cacheImage = new Image();

                // Set the landscape flag on load if the image is landscape
                $(cacheImage).load(function() {
                    // Set the image (now loaded) in the slide
                    //$('.slide:eq('+ index +') .img').css('background-image', 'url('+ this.src +')');
                    $('.slide:eq(' + index + ') .img').attr('src', this.src).unhide();
                });

                // Start the image load
                cacheImage.src = imgUrl;
            });
        };

        OF.page.requestGameDetail = function(game_id) {
            OF.api('/xp/games/' + game_id + '/descriptions/android', {
                on404: OF.page.abort,
                failure: function() {
                },
                success: function(data) {
                    OF.page.package_identifier = data.description.package_identifier;

                    $('#buy').html(OF.spotlight.buyButtonLabel(data.description.price, true));

                    $('.game_description').html(data.description.description.replace(/\\n/g, '<br>'));

                    if (OF.isDevice) {
                        OF.action('isApplicationInstalled', {
                            package_name: data.description.package_identifier,
                            callback: 'OF.page.installCallback'
                        });
                    } else {
                        OF.page.installCallback(false);
                    }

                    if (data.description.screenshots.length > 0) {
                        $("#screenshots").unhide();
                        OF.page.renderThumbnails(data.description.screenshots);
                    }

                    if (data.description.youtube.video_id) {
                        if ($("#video_main").hasClass("hidden")) {
                            $("#video_main").unhide();
                        }
                        $('#video_main img').remove();
                        $('#video_main').prepend('<img src="' + data.description.screenshots[0].url + '" />');
                        $('#video_main').touch(function() {
                            OF.action('openYoutubePlayer', {video_id: data.description.youtube.video_id});
                        });
                    } else {
                        $("#video_main").addClass("hidden").hide();
                    }
                }
            });

            OF.api('/xp/games/' + game_id, {
                on404: OF.page.abort,
                failure: function() {
                },
                success: function(data) {
                    $('#game_info .icon').css('background-image', 'url(' + data.game.android_icon_url + ')');
                    $('#game_info .title').text(data.game.name);
                    $('#game_info .subtitle').text(data.game.developer_name);
                }
            });
        };

        $("div.game").touch(function() {
            OF.page.requestGameDetail($(this).attr('game_id'));
            $('.active').removeClass('active');
            $(this).addClass("active");
        });
    };

    OF.spotlight._dataReady = function() {
        $("div.game").removeAttr("data-href").data("href","");
        $("#game_details").unhide();
    }
})(jQuery);