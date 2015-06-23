#include <pebble.h>
#include "bus_layout.h"

// BEGIN AUTO-GENERATED UI CODE; DO NOT MODIFY
static Window *s_window;
static GFont s_res_gothic_24_bold;
static GFont s_res_gothic_14;
static GFont s_res_gothic_28_bold;
static GFont s_res_gothic_18_bold;
static GBitmap *s_res_bus_nodata;
static GBitmap *s_res_actionicon_next_white;
static GBitmap *s_res_actionicon_refresh_white;
static GBitmap *s_res_actionicon_previous_white;
static TextLayer *textlayer_bus_no;
static TextLayer *textlayer_busstop_name;
static TextLayer *textlayer_busstop_code;
static TextLayer *textlayer_arrive_now;
static TextLayer *textlayer_arrive_next;
static TextLayer *textlayer_nextbus_label;
static BitmapLayer *bitmaplayer_current;
static BitmapLayer *bitmaplayer_next;
static TextLayer *textlayer_debug;
static ActionBarLayer *actionbar_layer;
static TextLayer *textlayer_pages;

static void initialise_ui(void) {
  s_window = window_create();
  #ifndef PBL_SDK_3
    window_set_fullscreen(s_window, false);
  #endif
  
  s_res_gothic_24_bold = fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD);
  s_res_gothic_14 = fonts_get_system_font(FONT_KEY_GOTHIC_14);
  s_res_gothic_28_bold = fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD);
  s_res_gothic_18_bold = fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD);
  s_res_bus_nodata = gbitmap_create_with_resource(RESOURCE_ID_BUS_NODATA);
  s_res_actionicon_next_white = gbitmap_create_with_resource(RESOURCE_ID_ACTIONICON_NEXT_WHITE);
  s_res_actionicon_refresh_white = gbitmap_create_with_resource(RESOURCE_ID_ACTIONICON_REFRESH_WHITE);
  s_res_actionicon_previous_white = gbitmap_create_with_resource(RESOURCE_ID_ACTIONICON_PREVIOUS_WHITE);
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
  bitmaplayer_current = bitmap_layer_create(GRect(42, 77, 35, 10));
  bitmap_layer_set_bitmap(bitmaplayer_current, s_res_bus_nodata);
  layer_add_child(window_get_root_layer(s_window), (Layer *)bitmaplayer_current);
  
  // bitmaplayer_next
  bitmaplayer_next = bitmap_layer_create(GRect(42, 123, 35, 10));
  bitmap_layer_set_bitmap(bitmaplayer_next, s_res_bus_nodata);
  layer_add_child(window_get_root_layer(s_window), (Layer *)bitmaplayer_next);
  
  // textlayer_debug
  textlayer_debug = text_layer_create(GRect(4, 38, 114, 20));
  text_layer_set_background_color(textlayer_debug, GColorClear);
  text_layer_set_text(textlayer_debug, "Debug Button");
  text_layer_set_font(textlayer_debug, s_res_gothic_14);
  layer_add_child(window_get_root_layer(s_window), (Layer *)textlayer_debug);
  
  // actionbar_layer
  actionbar_layer = action_bar_layer_create();
  action_bar_layer_add_to_window(actionbar_layer, s_window);
  action_bar_layer_set_background_color(actionbar_layer, GColorBlack);
  action_bar_layer_set_icon(actionbar_layer, BUTTON_ID_UP, s_res_actionicon_next_white);
  action_bar_layer_set_icon(actionbar_layer, BUTTON_ID_SELECT, s_res_actionicon_refresh_white);
  action_bar_layer_set_icon(actionbar_layer, BUTTON_ID_DOWN, s_res_actionicon_previous_white);
  layer_add_child(window_get_root_layer(s_window), (Layer *)actionbar_layer);
  
  // textlayer_pages
  textlayer_pages = text_layer_create(GRect(3, 132, 116, 20));
  text_layer_set_background_color(textlayer_pages, GColorClear);
  text_layer_set_text(textlayer_pages, "1/100");
  text_layer_set_text_alignment(textlayer_pages, GTextAlignmentRight);
  text_layer_set_font(textlayer_pages, s_res_gothic_14);
  layer_add_child(window_get_root_layer(s_window), (Layer *)textlayer_pages);
}

static void destroy_ui(void) {
  window_destroy(s_window);
  text_layer_destroy(textlayer_bus_no);
  text_layer_destroy(textlayer_busstop_name);
  text_layer_destroy(textlayer_busstop_code);
  text_layer_destroy(textlayer_arrive_now);
  text_layer_destroy(textlayer_arrive_next);
  text_layer_destroy(textlayer_nextbus_label);
  bitmap_layer_destroy(bitmaplayer_current);
  bitmap_layer_destroy(bitmaplayer_next);
  text_layer_destroy(textlayer_debug);
  action_bar_layer_destroy(actionbar_layer);
  text_layer_destroy(textlayer_pages);
  gbitmap_destroy(s_res_bus_nodata);
  gbitmap_destroy(s_res_actionicon_next_white);
  gbitmap_destroy(s_res_actionicon_refresh_white);
  gbitmap_destroy(s_res_actionicon_previous_white);
}
// END AUTO-GENERATED UI CODE

/*        INITIALIZE VARIABLES       */
static GBitmap *bus_available;
static GBitmap *bus_limited;
static GBitmap *bus_full;

static int current = -1;
static bool debugMode = false;

enum {
  KEY_BUTTON_EVENT = 0,
  BUTTON_PREVIOUS = 1,
  BUTTON_NEXT = 2,
  BUTTON_REFRESH = 3,
  MESSAGE_DATA_EVENT = 4,
  ESTIMATE_ARR_CURRENT_DATA = 5,
  ESTIMATE_ARR_NEXT_DATA = 6,
  ESTIMATE_LOAD_CURRENT_DATA = 7,
  ESTIMATE_LOAD_NEXT_DATA = 8,
  MESSAGE_ROAD_NAME = 9,
  MESSAGE_ROAD_CODE = 10,
  MESSAGE_BUS_SERVICE = 11,
  MESSAGE_CURRENT_FAV = 12,
  MESSAGE_MAX_FAV = 13
};

/*            APP MESSAGE             */

//Updates Load Image (1 - current, 2 - next)
static void updateLoad(int load, int l){
  switch (l){
    case 1:
    switch (load){
      case 0: bitmap_layer_set_bitmap(bitmaplayer_current, s_res_bus_nodata); break;
      case 1: bitmap_layer_set_bitmap(bitmaplayer_current, bus_available); break;
      case 2: bitmap_layer_set_bitmap(bitmaplayer_current, bus_limited); break;
      case 3: bitmap_layer_set_bitmap(bitmaplayer_current, bus_full); break;  
    }
    break;
    case 2:
    switch (load){
      case 0: bitmap_layer_set_bitmap(bitmaplayer_next, s_res_bus_nodata); break;
      case 1: bitmap_layer_set_bitmap(bitmaplayer_next, bus_available); break;
      case 2: bitmap_layer_set_bitmap(bitmaplayer_next, bus_limited); break;
      case 3: bitmap_layer_set_bitmap(bitmaplayer_next, bus_full); break;
    }
    break;
  }
}

char *translate_error(AppMessageResult result) {
  switch (result) {
    case APP_MSG_OK: return "APP_MSG_OK";
    case APP_MSG_SEND_TIMEOUT: return "APP_MSG_SEND_TIMEOUT";
    case APP_MSG_SEND_REJECTED: return "APP_MSG_SEND_REJECTED";
    case APP_MSG_NOT_CONNECTED: return "APP_MSG_NOT_CONNECTED";
    case APP_MSG_APP_NOT_RUNNING: return "APP_MSG_APP_NOT_RUNNING";
    case APP_MSG_INVALID_ARGS: return "APP_MSG_INVALID_ARGS";
    case APP_MSG_BUSY: return "APP_MSG_BUSY";
    case APP_MSG_BUFFER_OVERFLOW: return "APP_MSG_BUFFER_OVERFLOW";
    case APP_MSG_ALREADY_RELEASED: return "APP_MSG_ALREADY_RELEASED";
    case APP_MSG_CALLBACK_ALREADY_REGISTERED: return "APP_MSG_CALLBACK_ALREADY_REGISTERED";
    case APP_MSG_CALLBACK_NOT_REGISTERED: return "APP_MSG_CALLBACK_NOT_REGISTERED";
    case APP_MSG_OUT_OF_MEMORY: return "APP_MSG_OUT_OF_MEMORY";
    case APP_MSG_CLOSED: return "APP_MSG_CLOSED";
    case APP_MSG_INTERNAL_ERROR: return "APP_MSG_INTERNAL_ERROR";
    default: return "UNKNOWN ERROR";
  }
}

static void inbox_dropped_callback(AppMessageResult reason, void *context) {
  APP_LOG(APP_LOG_LEVEL_ERROR, "Message dropped! Reason: %i - %s", reason, translate_error(reason));
}

static void outbox_failed_callback(DictionaryIterator *iterator, AppMessageResult reason, void *context) {
  APP_LOG(APP_LOG_LEVEL_ERROR, "Outbox send failed! Reason: %i - %s", reason, translate_error(reason));
}

static void outbox_sent_callback(DictionaryIterator *iterator, void *context) {
  APP_LOG(APP_LOG_LEVEL_INFO, "Outbox send success!");
}

// App Message API
static void in_received_handler(DictionaryIterator *iter, void *context){
  APP_LOG(APP_LOG_LEVEL_INFO, "Message received!");
  // Get the first pair
  Tuple *t = dict_read_first(iter);
  // Long lived buffers
  static char roadName_buffer[255];
  static char roadCode_buffer[10];
  static char busService_buffer[10];
  static int max, loadC, loadN;
  static char arrC_data_buffer[10];
  static char arrN_data_buffer[10];
  static bool pgLoad = false, pgMaxLoad = false;

  // Process all pairs present
  while(t != NULL) {
    // Process this pair's key
    switch (t->key) {
      case MESSAGE_DATA_EVENT:
        APP_LOG(APP_LOG_LEVEL_INFO, "MESSAGE_DATA received with value %d", (int)t->value->int32);
        break;
      case MESSAGE_ROAD_NAME:
        snprintf(roadName_buffer, sizeof(roadName_buffer), "%s", t->value->cstring);
        text_layer_set_text(textlayer_busstop_name, roadName_buffer);
        break;
      case MESSAGE_ROAD_CODE:
        snprintf(roadCode_buffer, sizeof(roadCode_buffer), "%s", t->value->cstring);
        text_layer_set_text(textlayer_busstop_code, roadCode_buffer);
        break;
      case MESSAGE_BUS_SERVICE:
        snprintf(busService_buffer, sizeof(busService_buffer), "%s", t->value->cstring);
        text_layer_set_text(textlayer_bus_no, busService_buffer);
        break;
      case ESTIMATE_ARR_CURRENT_DATA:
        snprintf(arrC_data_buffer, sizeof(arrC_data_buffer), "%s", t->value->cstring);
        text_layer_set_text(textlayer_arrive_now, arrC_data_buffer);
        break;
      case ESTIMATE_ARR_NEXT_DATA:
        snprintf(arrN_data_buffer, sizeof(arrN_data_buffer), "%s", t->value->cstring);
        text_layer_set_text(textlayer_arrive_next, arrN_data_buffer);
        break;
      
      case MESSAGE_CURRENT_FAV:
        current = t->value->int32;
        pgLoad = true;
        break;
      case MESSAGE_MAX_FAV:
        max = t->value->int32;
        pgMaxLoad = true;
        break;
      
      case ESTIMATE_LOAD_CURRENT_DATA:
        loadC = t->value->int32;
        updateLoad(loadC, 1);
        break;
      case ESTIMATE_LOAD_NEXT_DATA:
        loadN = t->value->int32;
        updateLoad(loadN, 2);
        break;
    }
    
    //Handle paging
    static char paging_buffer[10];
    if (pgLoad && pgMaxLoad){
      snprintf(paging_buffer, sizeof(paging_buffer), "%d/%d", current, max);
    }

    // Get next pair, if any
    t = dict_read_next(iter);
  }
}

// Send command
void send_int(uint8_t key, uint8_t cmd)
{
  APP_LOG(APP_LOG_LEVEL_INFO, "Sending refresh data to phone");
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
      
    Tuplet value = TupletInteger(key, cmd);
    dict_write_tuplet(iter, &value);
      
    app_message_outbox_send();
}

// Next or previous
void go_next_or_prev(uint8_t key, uint8_t cmd){
  APP_LOG(APP_LOG_LEVEL_INFO, "Sending data to phone");
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);
      
  Tuplet value = TupletInteger(key, cmd);
  dict_write_tuplet(iter, &value);
  
  if (current != -1){
    Tuplet page = TupletInteger(MESSAGE_CURRENT_FAV, current);
    dict_write_tuplet(iter, &page);
  }
  
  app_message_outbox_send();
}

/*            USER ACTIONS           */
// When the select button is clicked
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (debugMode)
    text_layer_set_text(textlayer_debug, "Select (Refreshes)");
  // Tell android to refresh app
  send_int(KEY_BUTTON_EVENT, BUTTON_REFRESH);
}

// When the up button is clicked
static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (debugMode)
    text_layer_set_text(textlayer_debug, "Up (Go Previous)");
  // Go to previous if available
  go_next_or_prev(KEY_BUTTON_EVENT, BUTTON_PREVIOUS);
}

// When the down button is clicked
static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (debugMode)
    text_layer_set_text(textlayer_debug, "Down (Go Next)");
  // Go to next if available
  go_next_or_prev(KEY_BUTTON_EVENT, BUTTON_NEXT);
}

// The Button Config
static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

static void init_bus_indicators(void){
  bus_available = gbitmap_create_with_resource(RESOURCE_ID_BUS_SEATSAVAIL);
  bus_limited = gbitmap_create_with_resource(RESOURCE_ID_BUS_SEATSLIMITED);
  bus_full = gbitmap_create_with_resource(RESOURCE_ID_BUS_SEATSNONE);
}

static void handle_window_unload(Window* window) {
  destroy_ui();
}

void show_bus_layout(void) {
  initialise_ui();
  init_bus_indicators();
  
  action_bar_layer_set_click_config_provider(actionbar_layer, click_config_provider);
  window_set_window_handlers(s_window, (WindowHandlers) {
    .unload = handle_window_unload,
  });
  window_stack_push(s_window, true);
  
  APP_LOG(APP_LOG_LEVEL_INFO, "Inbox Max Size: %lu", app_message_inbox_size_maximum());
  
  //Register AppMessage events
  app_message_register_inbox_received(in_received_handler);
  app_message_register_inbox_dropped(inbox_dropped_callback);
  app_message_register_outbox_failed(outbox_failed_callback);
  app_message_register_outbox_sent(outbox_sent_callback);
  app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());    //Large input and output buffer sizes
  
  //Do the initial refresh of bus data and hides debug layer
  send_int(KEY_BUTTON_EVENT, BUTTON_REFRESH);
  
  //If debug mode off, hide it
  if (!debugMode)
    text_layer_set_text(textlayer_debug, "");
}

void hide_bus_layout(void) {
  window_stack_remove(s_window, true);
}
