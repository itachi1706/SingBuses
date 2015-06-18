#include <pebble.h>
#include "bus_layout.h"

// BEGIN AUTO-GENERATED UI CODE; DO NOT MODIFY
static Window *s_window;
static GFont s_res_gothic_24_bold;
static GFont s_res_gothic_14;
static GFont s_res_gothic_28_bold;
static GFont s_res_gothic_18_bold;
static GBitmap *s_res_action_previous_icon_white;
static GBitmap *s_res_action_refresh_icon_white;
static GBitmap *s_res_action_next_icon_white;
static ActionBarLayer *actionbar_layer;
static TextLayer *textlayer_bus_no;
static TextLayer *textlayer_busstop_name;
static TextLayer *textlayer_busstop_code;
static TextLayer *textlayer_arrive_now;
static TextLayer *textlayer_arrive_next;
static TextLayer *s_textlayer_1;
static BitmapLayer *bitmaplayer_current;
static BitmapLayer *bitmaplayer_next;

static void initialise_ui(void) {
  s_window = window_create();
  #ifndef PBL_SDK_3
    window_set_fullscreen(s_window, false);
  #endif
  
  s_res_gothic_24_bold = fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD);
  s_res_gothic_14 = fonts_get_system_font(FONT_KEY_GOTHIC_14);
  s_res_gothic_28_bold = fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD);
  s_res_gothic_18_bold = fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD);
  s_res_action_previous_icon_white = gbitmap_create_with_resource(RESOURCE_ID_ACTION_PREVIOUS_ICON_WHITE);
  s_res_action_refresh_icon_white = gbitmap_create_with_resource(RESOURCE_ID_ACTION_REFRESH_ICON_WHITE);
  s_res_action_next_icon_white = gbitmap_create_with_resource(RESOURCE_ID_ACTION_NEXT_ICON_WHITE);
  // actionbar_layer
  actionbar_layer = action_bar_layer_create();
  action_bar_layer_add_to_window(actionbar_layer, s_window);
  action_bar_layer_set_background_color(actionbar_layer, GColorBlack);
  action_bar_layer_set_icon(actionbar_layer, BUTTON_ID_UP, s_res_action_previous_icon_white);
  action_bar_layer_set_icon(actionbar_layer, BUTTON_ID_SELECT, s_res_action_refresh_icon_white);
  action_bar_layer_set_icon(actionbar_layer, BUTTON_ID_DOWN, s_res_action_next_icon_white);
  layer_add_child(window_get_root_layer(s_window), (Layer *)actionbar_layer);
  
  // textlayer_bus_no
  textlayer_bus_no = text_layer_create(GRect(3, 1, 51, 26));
  text_layer_set_background_color(textlayer_bus_no, GColorClear);
  text_layer_set_text(textlayer_bus_no, "9999X");
  text_layer_set_font(textlayer_bus_no, s_res_gothic_24_bold);
  layer_add_child(window_get_root_layer(s_window), (Layer *)textlayer_bus_no);
  
  // textlayer_busstop_name
  textlayer_busstop_name = text_layer_create(GRect(4, 25, 119, 20));
  text_layer_set_background_color(textlayer_busstop_name, GColorClear);
  text_layer_set_text(textlayer_busstop_name, "Rainbow Rd");
  layer_add_child(window_get_root_layer(s_window), (Layer *)textlayer_busstop_name);
  
  // textlayer_busstop_code
  textlayer_busstop_code = text_layer_create(GRect(60, 2, 59, 20));
  text_layer_set_background_color(textlayer_busstop_code, GColorClear);
  text_layer_set_text(textlayer_busstop_code, "9999999");
  text_layer_set_text_alignment(textlayer_busstop_code, GTextAlignmentRight);
  text_layer_set_font(textlayer_busstop_code, s_res_gothic_14);
  layer_add_child(window_get_root_layer(s_window), (Layer *)textlayer_busstop_code);
  
  // textlayer_arrive_now
  textlayer_arrive_now = text_layer_create(GRect(23, 49, 79, 31));
  text_layer_set_background_color(textlayer_arrive_now, GColorClear);
  text_layer_set_text(textlayer_arrive_now, "600 min");
  text_layer_set_text_alignment(textlayer_arrive_now, GTextAlignmentCenter);
  text_layer_set_font(textlayer_arrive_now, s_res_gothic_28_bold);
  layer_add_child(window_get_root_layer(s_window), (Layer *)textlayer_arrive_now);
  
  // textlayer_arrive_next
  textlayer_arrive_next = text_layer_create(GRect(28, 104, 68, 24));
  text_layer_set_background_color(textlayer_arrive_next, GColorClear);
  text_layer_set_text(textlayer_arrive_next, "600 min");
  text_layer_set_text_alignment(textlayer_arrive_next, GTextAlignmentCenter);
  text_layer_set_font(textlayer_arrive_next, s_res_gothic_18_bold);
  layer_add_child(window_get_root_layer(s_window), (Layer *)textlayer_arrive_next);
  
  // s_textlayer_1
  s_textlayer_1 = text_layer_create(GRect(4, 93, 100, 20));
  text_layer_set_background_color(s_textlayer_1, GColorClear);
  text_layer_set_text(s_textlayer_1, "Next Bus:");
  layer_add_child(window_get_root_layer(s_window), (Layer *)s_textlayer_1);
  
  // bitmaplayer_current
  bitmaplayer_current = bitmap_layer_create(GRect(23, 72, 75, 21));
  layer_add_child(window_get_root_layer(s_window), (Layer *)bitmaplayer_current);
  
  // bitmaplayer_next
  bitmaplayer_next = bitmap_layer_create(GRect(30, 122, 65, 18));
  layer_add_child(window_get_root_layer(s_window), (Layer *)bitmaplayer_next);
}

static void destroy_ui(void) {
  window_destroy(s_window);
  action_bar_layer_destroy(actionbar_layer);
  text_layer_destroy(textlayer_bus_no);
  text_layer_destroy(textlayer_busstop_name);
  text_layer_destroy(textlayer_busstop_code);
  text_layer_destroy(textlayer_arrive_now);
  text_layer_destroy(textlayer_arrive_next);
  text_layer_destroy(s_textlayer_1);
  bitmap_layer_destroy(bitmaplayer_current);
  bitmap_layer_destroy(bitmaplayer_next);
  gbitmap_destroy(s_res_action_previous_icon_white);
  gbitmap_destroy(s_res_action_refresh_icon_white);
  gbitmap_destroy(s_res_action_next_icon_white);
}
// END AUTO-GENERATED UI CODE

static void handle_window_unload(Window* window) {
  destroy_ui();
}

void show_bus_layout(void) {
  initialise_ui();
  window_set_window_handlers(s_window, (WindowHandlers) {
    .unload = handle_window_unload,
  });
  window_stack_push(s_window, true);
}

void hide_bus_layout(void) {
  window_stack_remove(s_window, true);
}
