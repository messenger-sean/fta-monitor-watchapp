#include <pebble.h>
#include "utility.h"
  
#define RED1 1
#define RED2 2
#define RED3 3
#define BLUE1 4
#define BLUE2 5
#define BLUE3 6
#define VIBE 7  
#define UPDATE 8
#define CHECK_TYPE(type, size) if (type != TUPLE_UINT && size != 1) { \
        APP_LOG(APP_LOG_LEVEL_ERROR, "Received nonuint type %d", (int) type); \
        t = dict_read_next(iter); \
        continue; \
                                                 }
  
// Constant text strings for the connection statuses
const char *eth = "Eth", *ds = "DS", *radio = "Rd", *rio = "RIO", *code = "Cd", *estop = "Est", *good = "G", *bwu = "BWU", *byp = "BYP";
  
static Window *s_main_window;
static Layer *s_grid_layer;
static TextLayer *s_red_header;
static TextLayer *s_blue_header;
static TextLayer *s_red1;
static TextLayer *s_red2;
static TextLayer *s_red3;
static TextLayer *s_blue1;
static TextLayer *s_blue2;
static TextLayer *s_blue3;
static GFont *s_source_code_pro;

typedef enum {
  ETH=0, DS=1, RADIO=2, RIO=3, CODE=4, ESTOP=5, GOOD=6, BWU=7, BYP = 8
} status_type;

void set_alliance_status(status_type status, uint8_t alliance, uint8_t team);

// Callback for receiving a message
static void inbox_received_callback(DictionaryIterator *iter, void *ctx) {
  Tuple *t = dict_read_first(iter);
  while (t != NULL) {
    if (t->type != TUPLE_UINT && t->length != 1) {
        APP_LOG(APP_LOG_LEVEL_ERROR, "Received nonuint type %d", (int) t->type);
        t = dict_read_next(iter);
        continue;
    }
    uint8_t status = t->value->uint8;
    switch (t->key) {
      case RED1:
      set_alliance_status((status_type) status, 1, 1);
      break;
      case RED2:
      set_alliance_status((status_type) status, 1, 2);
      break;
      case RED3:
      set_alliance_status((status_type) status, 1, 3);
      break;
      case BLUE1:
      set_alliance_status((status_type) status, 2, 1);
      break;
      case BLUE2:
      set_alliance_status((status_type) status, 2, 2);
      break;
      case BLUE3:
      set_alliance_status((status_type) status, 2, 3);
      break;
      case VIBE:
      APP_LOG(APP_LOG_LEVEL_INFO, "Vibrating");
      vibes_short_pulse();
      break;
    }
    
    t = dict_read_next(iter);
  }
}

// Requests an update from the companion app, if it's available
void request_update() {
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);
  dict_write_uint8(iter, UPDATE, 0);
  app_message_outbox_send();
}

// Single click callback
void select_single_click_handler(ClickRecognizerRef recognizer, void *context) {
  APP_LOG(APP_LOG_LEVEL_INFO, "In single click handler");
  request_update();
}

// ClickConfigProvider for the refresh button
void config_provider(Window *window) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_single_click_handler);
}

static void message_dropped_callback(AppMessageResult reason, void *ctx) {
  APP_LOG(APP_LOG_LEVEL_WARNING, "Message Dropped: %s", translate_error(reason));
}

static void outbox_failed_callback(DictionaryIterator *iter, AppMessageResult reason, void *ctx) {
  APP_LOG(APP_LOG_LEVEL_WARNING, "Outbox Send Failed: %s", translate_error(reason));
}

static void outbox_send_callback(DictionaryIterator *iter, void *ctx) {
  APP_LOG(APP_LOG_LEVEL_INFO, "Outbox Send Success!");
}

static void canvas_update_proc(Layer *this_layer, GContext *ctx) {
  // Draw a line down the middle of the screen
  graphics_draw_line(ctx, GPoint(74, 0), GPoint(74, 168));
  
  // Draw the alliance line
  graphics_draw_line(ctx, GPoint(0, 20), GPoint(144, 20));
  
  // Draw the 2 actual alliance dividers. They are (168-20-10)/3 pixels apart, which is 46.
  graphics_draw_line(ctx, GPoint(0, 66), GPoint(144, 66));
  graphics_draw_line(ctx, GPoint(0, 112), GPoint(144, 112));
}

void set_alliance_text(const char *text, bool hi_contrast, uint8_t alliance, uint8_t team) {
  uint16_t switch_mult = (alliance << 8) | team;
  TextLayer *team_layer;
  switch (switch_mult) {
    case 0x0101:
    team_layer = s_red1;
    break;
    case 0x0102:
    team_layer = s_red2;
    break;
    case 0x0103:
    team_layer = s_red3;
    break;
    case 0x0201:
    team_layer = s_blue1;
    break;
    case 0x0202:
    team_layer = s_blue2;
    break;
    case 0x0203:
    team_layer = s_blue3;
    break;
    default:
    return;
  }
 
  text_layer_set_text(team_layer, text);
  if (hi_contrast) {
    text_layer_set_background_color(team_layer, GColorBlack);
    text_layer_set_text_color(team_layer, GColorWhite);
  } else {
    text_layer_set_background_color(team_layer, GColorWhite);
    text_layer_set_text_color(team_layer, GColorBlack);
  }
}

// Sets the status of an alliance based on the given status type
void set_alliance_status(status_type status, uint8_t alliance, uint8_t team) {
  // If the app has told us to vibrate, then vibrate
  switch (status) {
    case ETH:
    set_alliance_text(eth, true, alliance, team);
    break;
    case DS:
    set_alliance_text(ds, true, alliance, team);
    break;
    case RADIO:
    set_alliance_text(radio, true, alliance, team);
    break;
    case RIO:
    set_alliance_text(rio, true, alliance, team);
    break;
    case CODE:
    set_alliance_text(code, true, alliance, team);
    break;
    case ESTOP:
    set_alliance_text(estop, true, alliance, team);
    break;
    case GOOD:
    set_alliance_text(good, false, alliance, team);
    break;
    case BWU:
    set_alliance_text(bwu, true, alliance, team);
    break;
    case BYP:
    set_alliance_text(byp, false, alliance, team);
    break;
  }
}

void setup_alliance_textlayer(TextLayer **layer, Layer *parent, int x, int y) {
  *layer = text_layer_create(GRect(x, y, 74, 46));
  text_layer_set_text_alignment(*layer, GTextAlignmentCenter);
  text_layer_set_font(*layer, s_source_code_pro);
  layer_add_child(parent, text_layer_get_layer(*layer));
}

static void main_window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);
  
  // Create the grid layer
  s_grid_layer = layer_create(GRect(0, 0, bounds.size.w, bounds.size.h));
  layer_add_child(window_layer, s_grid_layer);
  
  // Set the grid drawing update procedure
  layer_set_update_proc(s_grid_layer, canvas_update_proc);
  
  // Draw the Red and Blue text for the alliance headers
  s_red_header = text_layer_create(GRect(74, 0, 74, 20));
  s_blue_header = text_layer_create(GRect(0, 0, 74, 20));
  text_layer_set_background_color(s_red_header, GColorClear);
  text_layer_set_background_color(s_blue_header, GColorClear);
  text_layer_set_text_color(s_red_header, GColorBlack);
  text_layer_set_text_color(s_blue_header, GColorBlack);
  text_layer_set_text_alignment(s_red_header, GTextAlignmentCenter);
  text_layer_set_text_alignment(s_blue_header, GTextAlignmentCenter);
  text_layer_set_font(s_red_header, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_font(s_blue_header, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_text(s_red_header, "Red");
  text_layer_set_text(s_blue_header, "Blue");
  layer_add_child(window_layer, text_layer_get_layer(s_red_header));
  layer_add_child(window_layer, text_layer_get_layer(s_blue_header));
  
  // Create the layers for the different alliance statuses
  
  setup_alliance_textlayer(&s_red1, window_layer, 74, 112);
  setup_alliance_textlayer(&s_red2, window_layer, 74, 66);
  setup_alliance_textlayer(&s_red3, window_layer, 74, 20);
  setup_alliance_textlayer(&s_blue1, window_layer, 0, 20);
  setup_alliance_textlayer(&s_blue2, window_layer, 0, 66);
  setup_alliance_textlayer(&s_blue3, window_layer, 0, 112);
  set_alliance_status(ETH, 1, 1);
  set_alliance_status(ETH, 1, 2);
  set_alliance_status(ETH, 1, 3);
  set_alliance_status(ETH, 2, 1);
  set_alliance_status(ETH, 2, 2);
  set_alliance_status(ETH, 2, 3);
}

static void main_window_unload(Window *window) {
  layer_destroy(s_grid_layer);
  text_layer_destroy(s_red_header);
  text_layer_destroy(s_blue_header);
  text_layer_destroy(s_red1);
  text_layer_destroy(s_red2);
  text_layer_destroy(s_red3);
  text_layer_destroy(s_blue1);
  text_layer_destroy(s_blue2);
  text_layer_destroy(s_blue3);
}

static void init() {
  s_source_code_pro = fonts_load_custom_font(resource_get_handle(RESOURCE_ID_SOURCE_CODE_PRO_REG_38));
  s_main_window = window_create();
  window_set_window_handlers(s_main_window, (WindowHandlers) {
    .load = main_window_load,
    .unload = main_window_unload
  });
  
  window_stack_push(s_main_window, true);
  
  // Set up AppMessage so we are ready to receive data from the phone
  app_message_register_inbox_received(inbox_received_callback);
  app_message_register_inbox_dropped(message_dropped_callback);
  app_message_register_outbox_sent(outbox_send_callback);
  app_message_register_outbox_failed(outbox_failed_callback);
  app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());
  request_update();
  window_set_click_config_provider(s_main_window, (ClickConfigProvider) config_provider);
}

static void deinit() {
  window_destroy(s_main_window);
  fonts_unload_custom_font(s_source_code_pro);
  app_message_deregister_callbacks();
}

int main() {
  init();
  app_event_loop();
  deinit();
}