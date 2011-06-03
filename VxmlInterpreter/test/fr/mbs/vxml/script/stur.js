/**
* @namespace
* this libsup version.
*
* @example
* stur_version() // $HeadURL: http://sources.srvc.cvf/projects/p/plateformes_vocales/trunk/vxml/libsturjs/src/stur.js $ @ $Rev: 26786 $
*/ 
var stur_version = function() {
  return "$HeadURL: http://sources.srvc.cvf/projects/p/plateformes_vocales/trunk/vxml/libsturjs/src/stur.js $ @ $Rev: 26786 $"
}

//------------------------|
// Sup decoding function  |
//------------------------|

/**
 * @namespace
 *
 * decodes the possibility from the lastsup field as given by OMS.
 *
 * @description Returns an object with accessor for possibility fields.
 * The raw data of a possibility field may also be obtained using its type.
 * It can decode the stur embedded in a Q931 stream (lastsup as given by OMS is given).
 * It can directly decode a stur (data starts with 4700).
 * It can decode the stur embedded in some strange data by guessing where it is starting at 470083.
 *
 * @example
 * &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
 * &lt;vxml&gt;
 *   &lt;script src="js/stur.js"/&gt;
 * &lt;script&gt;
 * &lt;![CDATA[
 *  function lastsup() {
 *    var lastsup = "4700";
 *    if (typeof(session.connection.protocol.isdnvn6) != 'undefined'
 *        && typeof(session.connection.protocol.isdnvn6.lastsup) != 'undefined') {
 *      lastsup = session.connection.protocol.isdnvn6.lastsup;
 *    } else if (typeof(session.connection.protocol._isdn) != 'undefined' 
 *        && typeof(session.connection.protocol._isdn.lastsup) != 'undefined') {
 *      lastsup = session.connection.protocol._isdn.lastsup;
 *    }
 *    return lastsup;
 *  }
 *  ]]&gt;
 * &lt;/script&gt;
 *  &lt;form&gt;
 *    &lt;var name="sup" expr="stur_decode_possibility(lastsup())"/&gt;
 *    &lt;log&gt;info:called_nas          -> sup.called_nas"/&gt;
 *    &lt;log&gt;info:calling_nas         -> sup.calling_nas"/&gt;
 *    &lt;log&gt;info:ascii_context_data  -> sup.ascii_context_data"/&gt;
 *    &lt;log&gt;info:hexa_context_data   -> sup.hexa_context_data"/&gt;
 *    &lt;log&gt;info:return_context_data -> sup.return_context_data"/&gt;
 *    &lt;log&gt;info:origin_dept         -> sup.origin_dept"/&gt;
 *    &lt;log&gt;info:origin_number       -> sup.origin_number"/&gt;
 *    &lt;log&gt;info:origin_operator     -> sup.origin_operator"/&gt;
 *    &lt;log&gt;info:origin_country      -> sup.origin_country"/&gt;
 *  &lt;/form&gt;
 * &lt;/vxml&gt;
 *
 * @param {String} lastsup The lastsup as given by OMS.
 *
 * @author <a href="mailto:pgrange@cvf.fr">pgrange@cvf.fr</a>
 */
var stur_decode_possibility = function(lastsup) {
  var s = {
    /**
     * the called nas field of the decoded possibility or undefined if no called nas field was present in the possibility.
     *
     * @example
     * var p = stur_decode_possibility(lastsup)
     * var called_nas = s.called_nas
     *
     * @name stur_decode_possibility#called_nas
     * @field.
     */
      get called_nas() { if (this['83']) return decode_phone_number(this['83']) },

    /**
     * the calling nas field of the decoded possibility or undefined if no calling nas field was present in the possibility.
     *
     * @example
     * var p = stur_decode_possibility(lastsup)
     * var calling_nas = s.calling_nas
     *
     * @name stur_decode_possibility#calling_nas
     * @field.
     */
      get calling_nas() { if (this['89']) return decode_phone_number(this['89']) },

    /**
     * the freely encoded context data (85) field of the decoded possibility 
     * or undefined if no freely encoded context data was present in the possibility.
     *
     * @example
     * var p = stur_decode_possibility(lastsup)
     * var data = s.context_data
     *
     * @name stur_decode_possibility#context_data
     * @field.
     */
    get context_data() { return this["85"]},
    /**
     * the ASCII encoded context data (9504) field of the decoded possibility 
     * or undefined if no ASCII encoded context data was present in the possibility.
     * The value returned is a javascript String built from the ASCII data.
     *
     * @example
     * var lastsup = "4700" + "8316810105830783109229679106" +"950804"+"746F746F636865"
     * var p = stur_decode_possibility(lastsup)
     * var totoche = s.ascii_context_data //746F746F636865 -> totoche
     *
     * @name stur_decode_possibility#ascii_context_data
     * @field.
     */
    get ascii_context_data() {
      var context_data = this["95"]
      if (context_data) {
         var type = context_data.substr(0, 2)
         if (type == "04") {
           var data = context_data.substring(2)
           var ascii = ''
           for (var i = 0; i < data.length; i+=2) {
             ascii += String.fromCharCode(hexStringToInt(data[i] + data[i+1]))
           }
           return ascii
         }
      }
      return null
    },
    /**
     * the hexa context data (9500) field of the decoded possibility 
     * or undefined if no hexa context data was present in the possibility.
     *
     * @example
     * var p = stur_decode_possibility(lastsup)
     * var data = s.hexa_context_data
     *
     * @name stur_decode_possibility#hexa_context_data
     * @field.
     */
     get hexa_context_data() {
      var context_data = this["95"]
      if (context_data) {
         var type = context_data.substr(0, 2)
         if (type == "00") return context_data.substring(2)
      }
      return null
    },

     /**
     * the return context data (86) field of the decoded possibility 
     * or undefined if no return context data was present in the possibility.
     *
     * @example
     * var p = stur_decode_possibility(lastsup)
     * var data = s.return_context_data
     *
     * @name stur_decode_possibility#return_context_data
     * @field.
     */
    get return_context_data() { return this["86"] },

    /**
     * the french department (8A) from which the call originates or null if this information was not present in the possibility.
     *
     * @example
     * var p = stur_decode_possibility(lastsup)
     * var dept = p.origin_dept
     *
     * @name stur_decode_possibility#origin_dept
     * @field.
     */
    get origin_dept() { if (this["8A"]) return translate_dept(dcb_decode(this["8A"])) },

    /**
     * the phone number (8B) from which the call originates or null if this information was not present in the possibility.
     *
     * @example
     * var p = stur_decode_possibility(lastsup)
     * var dept = p.origin_number
     *
     * @name stur_decode_possibility#origin_number
     * @field.
     */
      get origin_number() { if (this['89']) return decode_phone_number(this["89"]) },

    /**
     * the operator (8C) from which the call originates or null if this information was not present in the possibility.
     *
     * @example
     * var p = stur_decode_possibility(lastsup)
     * var dept = p.origin_operator
     *
     * @name stur_decode_possibility#origin_operator
     * @field.
     */
    get origin_operator() { return this["8C"] },
    /**
     * the country code (9A) from which the call originates or null if this information was not present in the possibility.
     *
     * @example
     * var p = stur_decode_possibility(lastsup)
     * var dept = p.origin_country
     *
     * @name stur_decode_possibility#origin_country
     * @field.
     */
    get origin_country() { return this["9A"] }
  }  
  var stur = stur_decode_lastsup(lastsup)
  if (stur && stur['83']) tlv_decode(stur['83'], s)

  return s
}


//------------------------|
// Sup creation functions |
//------------------------|
 /**
  * @namespace stur_blind_transfer
  *
  * computes a blind transfer sup request and returns it as a string.
  * The blind transfer sup is computed using the given map.
  *
  * @description params is the map of parameters to use for this transfer.
  * All the parameters are optional but at least number is required if
  * you want your transfer request to work. It may contain the following fields:
  *
  * @example
  * number                -> the phone number to transfer to MUST NOT START
  *                          WITH 0 for french numbers
  * type                  -> the transfer type (defaults to "05")
  * transfer_forbidden    -> wether the target nas can or can not transfer again 
  *                          (defaults to false)
  * ascii_context_data    -> ascii context data to transmit (9504)
  *                          The string will be translated in hex representation 
  *                          of ascii characters codes
  * hexa_context_data     -> hexadecimal context data to transmit (9500)
  * context_data          -> the context_data to transmit (85)
  * return_data           -> the return data (hexa or ascii encoding format)
  * return_nas            -> the return nas
  * possibility_forbidden -> wether the target nas can or can not consult the 
  *                          possibility (defaults to false)
  * no_id_transmission    -> wether we want to transmit caller id to the target nas or 
  *                          not (defaults to false)
  * partial_id_transmission -> wether we want to only transmit partial caller id
  *                            information (only the department or country code 
  *                            is transmitted) or everything (defaults to false)
  *
  * Warning! Use at most one of *context_data or you may obtain an invalid transfer request.
  *
  * Warning! Use at most one of *_id_transmission or you may obtain an invalid transfer request.
  *
  * Warning! This API do not remove the first '0' in number as libsup did. You must remove
  *          the first '0' for french route destinations. For instance to transfer to
  *          0556019017, use 556019017.
  *
  * @example
  * var sup = stur_blind_transfer({number: '556019017'})  //do not start with 0
  *
  * @example
  * var sup = stur_blind_transfer({number: '556019017',
  *                               type: '03',
  *                               hexa_context_data: '15C00123456789222000'})
  * @example 
  * &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
  * &lt;vxml&gt;
  *   &lt;script src="js/stur.js"/&gt;
  * 
  *   &lt;var name="transferSup" expr='stur_blind_transfer({number: "556019017", 
  *                                                        hexa_context_data: "15C00123456789222000"})'/&gt;
  *   &lt;form id="ReroutageLibSupv2"&gt;
  *     &lt;block&gt;
  *       &lt;log&gt;&lt;value expr="'sup:' + transferSup"/&gt;&lt;/log&gt;
  *     &lt;/block&gt;
  *     &lt;transfer name="myCall" 
  *               destexpr="'sup:' + transferSup" 
  *               bridge="true"  
  *               connecttimeout="200s"
  *               transferaudio="sons/1013+/welcome_10s.wav"&gt;
  *       &lt;filled&gt;
  *           &lt;log&gt;Dans le filled mycall=[&lt;value expr="myCall"/&gt;] &lt;/log&gt;
  *       &lt;/filled&gt;
  *     &lt;/transfer&gt;
  *      
  *     &lt;transfer name="myCall2" destexpr="'sup:' + stur_callback()" bridge="true" connecttimeout="1s"&gt;
  *       &lt;filled&gt;&lt;log&gt;Apres le retro appel [&lt;value expr="myCall2"/&gt;]&lt;/log&gt;&lt;/filled&gt;
  *     &lt;/transfer&gt;
  *  &lt;/form&gt;
  * &lt;/vxml&gt;
  *
  * @returns {String} The sup to use to run a blind_transfer. 
  * You have to prefix this with 'sup:' in your vxml page for OMS.
  */
var stur_blind_transfer = function(params) {
  return "4700" + tlv_encode("81", stur_internal_transfer_data(params));
}

 /**
  * @namespace stur_transfer
  *
  * computes a transfer sup request and returns it as a string.
  * The transfer sup is computed using the given map.
  *
  * @description params is the map of parameters to use for this transfer.
  * All the parameters are optional but at least number is required if
  * you want your transfer request to work. It may contain the following fields:
  *
  * @example
  * number                -> the phone number to transfer to MUST NOT START
  *                          WITH 0 for french numbers
  * type                  -> the transfer type (defaults to "05")
  * transfer_forbidden    -> wether the target nas can or can not transfer again 
  *                          (defaults to false)
  * ascii_context_data    -> the ascii_context_data to transmit (9504). 
  *                          The string will be translated in hex representation 
  *                          of ascii characters codes.
  * hexa_context_data     -> the hexa_context_data to transmit (9500).
  * context_data          -> the context_data to transmit (85)
  * return_data           -> the return data
  * return_nas            -> the return nas
  * possibility_forbidden -> wether the target nas can or can not consult the 
  *                          possibility (defaults to false)
  * no_id_transmission    -> wether we want to transmit caller id to the target nas or 
  *                          not (defaults to false)
  * partial_id_transmission -> wether we want to only transmit partial caller id
  *                            information (only the department or country code 
  *                            is transmitted) or everything (defaults to false)
  *
  * Warning! Use only one of *context_data or you may obtain an invalid transfer request.
  *
  * Warning! Use at most one of *_id_transmission or you may obtain an invalid transfer request.
  *
  * Warning! This API do not remove the first '0' in number as libsup did. You must remove
  *          the first '0' for french route destinations. For instance to transfer to
  *          0556019017, use 556019017.
  *
  * @example
  * var sup = stur_transfer({number: '556019017'}) //do not start with '0'
  *
  * @example
  * var sup = stur_transfer({number: '556019017',
  *                               type: '03',
  *                               context_data: '15C00123456789222000',
  *                               context_data_type: SUPv2.HEXA})
  * @returns {String} The sup to use to run a transfer. 
  *
  * @example 
  * &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
  * &lt;vxml&gt;
  *   &lt;script src="js/stur.js"/&gt;
  * 
  *   &lt;var name="transferSup" expr='stur_transfer({number: "556019017", 
  *                                                  ascii_context_data: "coucou"})'/&gt;
  *   &lt;form id="ReroutageLibSupv2"&gt;
  *     &lt;block&gt;
  *       &lt;log&gt;&lt;value expr="'sup:' + transferSup"/&gt;&lt;/log&gt;
  *     &lt;/block&gt;
  *     &lt;transfer name="myCall" 
  *               destexpr="'sup:' + transferSup" 
  *               bridge="true"  
  *               connecttimeout="200s"
  *               transferaudio="sons/1013+/welcome_10s.wav"&gt;
  *       &lt;filled&gt;
  *           &lt;log&gt;Dans le filled mycall=[&lt;value expr="myCall"/&gt;] &lt;/log&gt;
  *       &lt;/filled&gt;
  *     &lt;/transfer&gt;
  *      
  *     &lt;transfer name="myCall2" destexpr="'sup:' + stur_callback()" bridge="true" connecttimeout="1s"&gt;
  *       &lt;filled&gt;&lt;log&gt;Apres le retro appel [&lt;value expr="myCall2"/&gt;]&lt;/log&gt;&lt;/filled&gt;
  *     &lt;/transfer&gt;
  *  &lt;/form&gt;
  * &lt;/vxml&gt;
  *
  * You have to prefix this with 'sup:' in your vxml page for OMS.
  */
var stur_transfer = function(params) {
  return "4700" + tlv_encode("8A", stur_internal_transfer_data(params));
}

/**
 * @namespace stur_callback
 * computes a callback transfer sup request (rétro-appel) and returns it as a string.
 *
 * @example
 * var sup = stur_callback()
 *
 * @returns {String} The sup to use to run a callback transfer. 
 * You have to prefix this with 'sup:' in your vxml page for OMS.
 */
var stur_callback = function() {
  return "47008B00"
}

/**
 * @namespace stur_change_itx
 * computes a query itx change request (modification de taxation) and returns it as a string.
 * <a href="http://wiki.srvc.cvf/index.php/Tarification_vocale">voir la page wiki sur la tarification vocale</a>
 *
 * @example
 * var sup = stur_change_itx(68)
 *
 * @returns {String} The sup to request an itx change.
 * You have to prefix this with 'sup:' in your vxml page for OMS.
 */
var stur_change_itx = function(itx) {
  return "4700" + tlv_encode("85", tlv_encode("90", intToHex2DigitsString(itx)))
}

//-----------------------|
// Sup utility functions |
//-----------------------|
/**#nocode+*/

/**
 * @namespace
 * you should prefer stur_decode_possibility instead of this function.
 *
 * This function decodes the lastsup as given by OMS.
 * The result is a map wich keys are the tlv field types contained
 * in the lastsup and wich values are the corresponding values.
 *
 */
var q931_decode = function(lastsup) {
  // see specification here
  // Q931 : http://www.itu.int/rec/dologin_pub.asp?lang=f&id=T-REC-Q.931-199805-I!!PDF-F&type=items
  // FT variations : http://vocal.srvc.cvf/isdn/STI6_v3.pdf
  var remaining = lastsup
  var result = {}
  result.protocol_discriminator = remaining.substr(0,2)
  remaining = remaining.substr(2)
  var length = hexStringToInt(remaining.substr(0,2)) * 2
  result.call_reference = remaining.substr(2, length)
  remaining = remaining.substr(length + 2)
  result.message_type = remaining.substr(0,2)
  remaining = remaining.substr(2)
  while (remaining) {
    var type = remaining.substr(0,2)
    if (hexStringToInt(type.charAt(0)) & 8) {
      //single octet data
      result[type.charAt(0)] = type.charAt(1)
      remaining = remaining.substr(2)
    } else {
      //tlv like data
      length = hexStringToInt(remaining.substr(2,2)) * 2
      if (isNaN(length)) {
        //throw "invalid_q931: " + lastsup
        //we have some crappy data after the q931.
        //We ignore it
        break
      }

      result[type] = remaining.substr(4, length)
      remaining = remaining.substr(length + 4)
    }
  }
  return result
}

var stur_decode_lastsup = function(lastsup) {
  if (lastsup.substr(0, 4) == "4700") return tlv_decode(lastsup) //this is a stur, directly decode it
  if (lastsup.substr(0, 2) == "08") {
      var sup = q931_decode(lastsup)['77']
      if (sup) return  tlv_decode(sup) //Q931
      else return null
  }
  if (lastsup.indexOf("470083") != -1) return tlv_decode(lastsup.slice(lastsup.indexOf("470083") + 4)) //unknown so guessing
}

var stur_internal_transfer_data = function(params) {
  var transfer_data = ''
  var transfer_type = '05'

  if ('type' in params) transfer_type = params.type
  transfer_data += tlv_encode("81", transfer_type)

  if (params.transfer_forbidden) transfer_data += "8200"

  if ('number' in params) transfer_data += tlv_encode("83", encode_phone_number(params.number));

  if ('ascii_context_data' in params) {
    var hex_string = ''
    for (var i = 0; i < params.ascii_context_data.length; i++) {
      hex_string += (params.ascii_context_data.charCodeAt(i) & 0xFF).toString(16).toUpperCase()
    }
    transfer_data += tlv_encode("95", "04" + hex_string)
  } 

  if ('hexa_context_data' in params) transfer_data += tlv_encode("95", "00" + params.hexa_context_data);

  if ('context_data' in params) transfer_data += tlv_encode("85", params.context_data);

  if ('return_data' in params) transfer_data += tlv_encode("86", params.return_data)

  if ('return_nas' in params) transfer_data += tlv_encode("87", encode_phone_number(params.return_nas))

  if (params.possibility_forbidden) transfer_data += "9600"

  if (params.no_id_transmission) transfer_data += "9700"

  if (params.partial_id_transmission) transfer_data += "9800"

  return transfer_data
}

var tlv_decode = function(data, tlv_result) {
  if (! tlv_result) tlv_result = {}
  var remaining = data
  while (remaining.length > 0) {
    var type = remaining.substr(0, 2)
    var length = hexStringToInt(remaining.substr(2,2)) * 2
    if (isNaN(length)) {
      //throw "invalid_tlv: " + data
      //we have some crappy data after the sup.
      //We ignore it
      break
    }

    tlv_result[type] = remaining.substr(4, length)
    remaining = remaining.substr(length + 4)
  }
  return tlv_result
}

var intToHex2DigitsString = function(value) {
  var result = value.toString(16).toUpperCase();
  return result.length > 1 ? result : "0" + result;
}

var hexStringToInt = function(value) {
  return Number("0x" + value);
}

var tlv_encode = function (type, value) {
  return type + intToHex2DigitsString(value.length/2) + value;
}

var dcb_decode = function(value) {
  //dcb encodes numbers with digit
  //as half an octet in hexa and
  //padded with F
  //for instance 12043 is encoded  12043F
  if (value.charAt(value.length - 1) == 'F')
    return value.substr(0, value.length - 1)
  else
    return value
}

var dept_translation_table = {
  '101': '971',
  '102': '972',
  '103': '973',
  '104': '974',
  '105': '976',
  '106': '975'
}
var translate_dept = function(ri_dept) {
  //translate ri_dept codes to french dept
  //... especially for over seas
  var translated = dept_translation_table[ri_dept]
  if (translated) return translated
  else return ri_dept
}

var decode_phone_number = function(phone_number_data) {
  var PHONE_HEADERS_LENGTH=4;
  var parity = (eval(phone_number_data[0]) & 8) != 8;     
  var encoded_number = phone_number_data.slice(PHONE_HEADERS_LENGTH);
  var decoded_number="";
  
  for (var i=0; i<encoded_number.length; i+=2) {
    var car1 = encoded_number.charAt(i);
    var car2 = encoded_number.charAt(i+1);
    decoded_number = decoded_number + car2 + car1;
  }
  if (! parity) {
    decoded_number=decoded_number.slice(0,decoded_number.length-1)
  }
  return decoded_number;
}

var encode_phone_number = function(phone_number) {
  var encoded_number="";
  var parity = true;
  
  var len = phone_number.length;
  
  if((len % 2) == 1) {
    phone_number = phone_number + "0";
    parity = false;
  }
  
  for (var i=0; i<len; i+=2) {
    var car1 = phone_number.charAt(i);
    var car2 = phone_number.charAt(i+1);
    encoded_number = encoded_number + car2 + car1;
  }
  var first_octet = parity ? 0 : 8 //parity
  var second_octet = 3 //national number
  var third_octet = 0 //complete number
  third_octet |= 1 //RNIS
  var fourth_octet = 0
  return String(first_octet) + String(second_octet) 
    + String(third_octet)  + String(fourth_octet)
    + encoded_number
}

//---------------------
// PAC/RI src functions
//---------------------
var transfer_src = function() {
  if (typeof(mbs_transfer_url) == 'undefined') {
    throw "mbs_transfer_url transfer url has not been set on this plateform. See http://wiki.srvc.cvf/index.php/PAC for details"
  }
  return mbs_transfer_url
}

var possibilities_src = function() {
  if (typeof(mbs_possibilities_url) == 'undefined') {
    throw "mbs_possibilities_url possibilities url has not been set on this plateform. See http://wiki.srvc.cvf/index.php/PAC for details"
  }
  return mbs_possibilities_url
}


