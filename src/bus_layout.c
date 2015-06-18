#include <pebble.h>
#include "bus_layout.h"

// BEGIN AUTO-GENERATED UI CODE; DO NOT MODIFY
static Window *s_window;
static GBitmap *s_res_actionicon_previous_white;
static GBitmap *s_res_actionicon_next_white;
static GFont s_res_gothic_24_bold;
static GFont s_res_gothic_14;
static GFont s_res_gothic_28_bold;
static GFont s_res_gothic_18_bold;
static GBitmap *s_res_actionicon_refresh_white;
static ActionBarLayer *actionbar_layer;
static TextLayer *textlayer_bus_no;
static TextLayer *textlayer_busstop_name;
static TextLayer *textlayer_busstop_code;
static TextLayer *textlayer_arrive_now;
static TextLayer *textlayer_arrive_next;
static TextLayer *textlayer_nextbus_label;
static BitmapLayer *bitmaplayer_current;
static BitmapLayer *bitmaplayer_next;
static TextLayer *textlayer_debug;

static void initialise_ui(void) {
  s_window = window_create();
  #ifndef PBL_SDK_3
    window_set_fullscreen(s_window, false);
  #endif
  
  s_res_actionicon_previous_white = gbitmap_create_with_resource(RESOURCE_ID_ACTIONICON_PREVIOUS_WHITE);
  s_res_actionicon_next_white = gbitmap_create_with_resource(RESOURCE_ID_ACTIONICON_NEXT_WHITE);
  s_res_gothic_24_bold = fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD);
  s_res_gothic_14 = fonts_get_system_font(FONT_KEY_GOTHIC_14);
  s_res_gothic_28_bold = fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD);
  s_res_gothic_18_bold = fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD);
  s_res_actionicon_refresh_white = gbitmap_create_with_resource(RESOURCE_ID_ACTIONICON_REFRESH_WHITE);
  // actionbar_layer
  actionbar_layer = action_bar_layer_create();
  action_bar_layer_add_to_window(actionbar_layer, s_window);
  action_bar_layer_set_background_color(actionbar_layer, GColorBlack);
  action_bar_layer_set_icon(actionbar_layer, BUTTON_ID_UP, s_res_actionicon_previous_white);
  action_bar_layer_set_icon(actionbar_layer, BUTTON_ID_SELECT, s_res_actionicon_refresh_white);
  action_bar_layer_set_icon(actionbar_layer, BUTTON_ID_DOWN, s_res_actionicon_next_white);
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
  
  // textlayer_nextbus_label
  textlayer_nextbus_label = text_layer_create(GRect(4, 93, 100, 20));
  text_layer_set_background_color(textlayer_nextbus_label, GColorClear);
  text_layer_set_text(textlayer_nextbus_label, "Next Bus:");
  layer_add_child(window_get_root_layer(s_window), (Layer *)textlayer_nextbus_label);
  
  // bitmaplayer_current
  bitmaplayer_current = bitmap_layer_create(GRect(23, 72, 75, 21));
  layer_add_child(window_get_root_layer(s_window), (Layer *)bitmaplayer_current);
  
  // bitmaplayer_next
  bitmaplayer_next = bitmap_layer_create(GRect(30, 122, 65, 18));
  layer_add_child(window_get_root_layer(s_window), (Layer *)bitmaplayer_next);
  
  // textlayer_debug
  textlayer_debug = text_layer_create(GRect(3, 136, 117, 20));
  text_layer_set_text(textlayer_debug, "Debug Button");
  text_layer_set_font(textlayer_debug, s_res_gothic_14);
  layer_add_child(window_get_root_layer(s_window), (Layer *)textlayer_debug);
}

static void destroy_ui(void) {
  window_destroy(s_window);
  action_bar_layer_destroy(actionbar_layer);
  text_layer_destroy(textlayer_bus_no);
  text_layer_destroy(textlayer_busstop_name);
  text_layer_destroy(textlayer_busstop_code);
  text_layer_destroy(textlayer_arrive_now);
  text_layer_destroy(textlayer_arrive_next);
  text_layer_destroy(textlayer_nextbus_label);
  bitmap_layer_destroy(bitmaplayer_current);
  bitmap_layer_destroy(bitmaplayer_next);
  text_layer_destroy(textlayer_debug);
  gbitmap_destroy(s_res_actionicon_previous_white);
  gbitmap_destroy(s_res_actionicon_next_white);
  gbitmap_destroy(s_res_actionicon_refresh_white);
}
// END AUTO-GENERATED UI CODE

enum {
    KEY_BUTTON_EVENT = 0,
    BUTTON_PREVIOUS = 1,
    BUTTON_NEXT = 2,
    BUTTON_REFRESH = 3,
    MESSAGE_DATA = 4,
    MESSAGE_DATA_EVENT = 5
};

// App Message API
static void in_received_handler(DictionaryIterator *iter, void *context){
  //TODO Handle incoming messages
}

// Send command
void send_int(uint8_t key, uint8_t cmd)
{
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
      
    Tuplet value = TupletInteger(key, cmd);
    dict_write_tuplet(iter, &value);
      
    app_message_outbox_send();
}

//User Actions

// When the select button is clicked
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(textlayer_debug, "Select (Refreshes)");
  //TODO Tell android to refresh app
  send_int(KEY_BUTTON_EVENT, BUTTON_REFRESH);
}

// When the up button is clicked
static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(textlayer_debug, "Up (Go Previous)");
  //TODO Go to previous if available
  send_int(KEY_BUTTON_EVENT, BUTTON_PREVIOUS);
}

// When the down button is clicked
static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(textlayer_debug, "Down (Go Next)");
  //TODO Go to next if available
  send_int(KEY_BUTTON_EVENT, BUTTON_NEXT);
}

// The Button Config
static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

static void handle_window_unload(Window* window) {
  destroy_ui();
}

void show_bus_layout(void) {
  initialise_ui();
  action_bar_layer_set_click_config_provider(actionbar_layer, click_config_provider);
  window_set_window_handlers(s_window, (WindowHandlers) {
    .unload = handle_window_unload,
  });
  window_stack_push(s_window, true);
  
  //Register AppMessage events
  app_message_register_inbox_received(in_received_handler);           
  app_message_open(512, 512);    //Large input and output buffer sizes
}

void hide_bus_layout(void) {
  window_stack_remove(s_window, true);
}
