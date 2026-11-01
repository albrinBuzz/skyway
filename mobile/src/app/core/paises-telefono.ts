/**
 * Catálogo completo de códigos telefónicos internacionales (ITU-T E.164)
 * Ordenados alfabéticamente en español con bandera emoji, ISO-2, rangos y ejemplos.
 */

export interface PaisTelefono {
  iso2: string;
  prefijo: string;
  nombre: string;
  bandera: string;
  minDigitos: number;
  maxDigitos: number;
  ejemplo: string;
}

export const PAIS_POR_DEFECTO_ISO2 = 'CL';

export const PAISES_TELEFONO: PaisTelefono[] = [
  {
    "iso2": "AF",
    "prefijo": "+93",
    "nombre": "Afganistán",
    "bandera": "🇦🇫",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "701234567"
  },
  {
    "iso2": "AL",
    "prefijo": "+355",
    "nombre": "Albania",
    "bandera": "🇦🇱",
    "minDigitos": 8,
    "maxDigitos": 9,
    "ejemplo": "671234567"
  },
  {
    "iso2": "DE",
    "prefijo": "+49",
    "nombre": "Alemania",
    "bandera": "🇩🇪",
    "minDigitos": 10,
    "maxDigitos": 11,
    "ejemplo": "15112345678"
  },
  {
    "iso2": "AD",
    "prefijo": "+376",
    "nombre": "Andorra",
    "bandera": "🇦🇩",
    "minDigitos": 6,
    "maxDigitos": 9,
    "ejemplo": "312345"
  },
  {
    "iso2": "AO",
    "prefijo": "+244",
    "nombre": "Angola",
    "bandera": "🇦🇴",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "923123456"
  },
  {
    "iso2": "AI",
    "prefijo": "+1264",
    "nombre": "Anguila",
    "bandera": "🇦🇮",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "2351234"
  },
  {
    "iso2": "AG",
    "prefijo": "+1268",
    "nombre": "Antigua y Barbuda",
    "bandera": "🇦🇬",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "4641234"
  },
  {
    "iso2": "SA",
    "prefijo": "+966",
    "nombre": "Arabia Saudita",
    "bandera": "🇸🇦",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "501234567"
  },
  {
    "iso2": "DZ",
    "prefijo": "+213",
    "nombre": "Argelia",
    "bandera": "🇩🇿",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "551234567"
  },
  {
    "iso2": "AR",
    "prefijo": "+54",
    "nombre": "Argentina",
    "bandera": "🇦🇷",
    "minDigitos": 10,
    "maxDigitos": 11,
    "ejemplo": "1112345678"
  },
  {
    "iso2": "AM",
    "prefijo": "+374",
    "nombre": "Armenia",
    "bandera": "🇦🇲",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "77123456"
  },
  {
    "iso2": "AW",
    "prefijo": "+297",
    "nombre": "Aruba",
    "bandera": "🇦🇼",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "5601234"
  },
  {
    "iso2": "AU",
    "prefijo": "+61",
    "nombre": "Australia",
    "bandera": "🇦🇺",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "412345678"
  },
  {
    "iso2": "AT",
    "prefijo": "+43",
    "nombre": "Austria",
    "bandera": "🇦🇹",
    "minDigitos": 10,
    "maxDigitos": 11,
    "ejemplo": "6641234567"
  },
  {
    "iso2": "AZ",
    "prefijo": "+994",
    "nombre": "Azerbaiyán",
    "bandera": "🇦🇿",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "501234567"
  },
  {
    "iso2": "BS",
    "prefijo": "+1242",
    "nombre": "Bahamas",
    "bandera": "🇧🇸",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "3591234"
  },
  {
    "iso2": "BD",
    "prefijo": "+880",
    "nombre": "Bangladés",
    "bandera": "🇧🇩",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "1812345678"
  },
  {
    "iso2": "BB",
    "prefijo": "+1246",
    "nombre": "Barbados",
    "bandera": "🇧🇧",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "2301234"
  },
  {
    "iso2": "BH",
    "prefijo": "+973",
    "nombre": "Baréin",
    "bandera": "🇧🇭",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "36001234"
  },
  {
    "iso2": "BE",
    "prefijo": "+32",
    "nombre": "Bélgica",
    "bandera": "🇧🇪",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "470123456"
  },
  {
    "iso2": "BZ",
    "prefijo": "+501",
    "nombre": "Belice",
    "bandera": "🇧🇿",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "6221234"
  },
  {
    "iso2": "BJ",
    "prefijo": "+229",
    "nombre": "Benín",
    "bandera": "🇧🇯",
    "minDigitos": 8,
    "maxDigitos": 10,
    "ejemplo": "90012345"
  },
  {
    "iso2": "BM",
    "prefijo": "+1441",
    "nombre": "Bermudas",
    "bandera": "🇧🇲",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "5051234"
  },
  {
    "iso2": "BY",
    "prefijo": "+375",
    "nombre": "Bielorrusia",
    "bandera": "🇧🇾",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "291234567"
  },
  {
    "iso2": "BO",
    "prefijo": "+591",
    "nombre": "Bolivia",
    "bandera": "🇧🇴",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "71234567"
  },
  {
    "iso2": "BA",
    "prefijo": "+387",
    "nombre": "Bosnia y Herzegovina",
    "bandera": "🇧🇦",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "61123456"
  },
  {
    "iso2": "BW",
    "prefijo": "+267",
    "nombre": "Botsuana",
    "bandera": "🇧🇼",
    "minDigitos": 7,
    "maxDigitos": 8,
    "ejemplo": "71123456"
  },
  {
    "iso2": "BR",
    "prefijo": "+55",
    "nombre": "Brasil",
    "bandera": "🇧🇷",
    "minDigitos": 10,
    "maxDigitos": 11,
    "ejemplo": "11912345678"
  },
  {
    "iso2": "BN",
    "prefijo": "+673",
    "nombre": "Brunéi",
    "bandera": "🇧🇳",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7123456"
  },
  {
    "iso2": "BG",
    "prefijo": "+359",
    "nombre": "Bulgaria",
    "bandera": "🇧🇬",
    "minDigitos": 8,
    "maxDigitos": 9,
    "ejemplo": "871234567"
  },
  {
    "iso2": "BF",
    "prefijo": "+226",
    "nombre": "Burkina Faso",
    "bandera": "🇧🇫",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "70123456"
  },
  {
    "iso2": "BI",
    "prefijo": "+257",
    "nombre": "Burundi",
    "bandera": "🇧🇮",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "79123456"
  },
  {
    "iso2": "BT",
    "prefijo": "+975",
    "nombre": "Bután",
    "bandera": "🇧🇹",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "17123456"
  },
  {
    "iso2": "CV",
    "prefijo": "+238",
    "nombre": "Cabo Verde",
    "bandera": "🇨🇻",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "9912345"
  },
  {
    "iso2": "KH",
    "prefijo": "+855",
    "nombre": "Camboya",
    "bandera": "🇰🇭",
    "minDigitos": 8,
    "maxDigitos": 9,
    "ejemplo": "12123456"
  },
  {
    "iso2": "CM",
    "prefijo": "+237",
    "nombre": "Camerún",
    "bandera": "🇨🇲",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "671234567"
  },
  {
    "iso2": "CA",
    "prefijo": "+1",
    "nombre": "Canadá",
    "bandera": "🇨🇦",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "4165550123"
  },
  {
    "iso2": "QA",
    "prefijo": "+974",
    "nombre": "Catar",
    "bandera": "🇶🇦",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "33123456"
  },
  {
    "iso2": "TD",
    "prefijo": "+235",
    "nombre": "Chad",
    "bandera": "🇹🇩",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "66123456"
  },
  {
    "iso2": "CL",
    "prefijo": "+56",
    "nombre": "Chile",
    "bandera": "🇨🇱",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "912345678"
  },
  {
    "iso2": "CN",
    "prefijo": "+86",
    "nombre": "China",
    "bandera": "🇨🇳",
    "minDigitos": 11,
    "maxDigitos": 11,
    "ejemplo": "13812345678"
  },
  {
    "iso2": "CY",
    "prefijo": "+357",
    "nombre": "Chipre",
    "bandera": "🇨🇾",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "96123456"
  },
  {
    "iso2": "CO",
    "prefijo": "+57",
    "nombre": "Colombia",
    "bandera": "🇨🇴",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "3001234567"
  },
  {
    "iso2": "KM",
    "prefijo": "+269",
    "nombre": "Comoras",
    "bandera": "🇰🇲",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "3212345"
  },
  {
    "iso2": "KP",
    "prefijo": "+850",
    "nombre": "Corea del Norte",
    "bandera": "🇰🇵",
    "minDigitos": 8,
    "maxDigitos": 10,
    "ejemplo": "1912345678"
  },
  {
    "iso2": "KR",
    "prefijo": "+82",
    "nombre": "Corea del Sur",
    "bandera": "🇰🇷",
    "minDigitos": 9,
    "maxDigitos": 10,
    "ejemplo": "1012345678"
  },
  {
    "iso2": "CI",
    "prefijo": "+225",
    "nombre": "Costa de Marfil",
    "bandera": "🇨🇮",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "0701234567"
  },
  {
    "iso2": "CR",
    "prefijo": "+506",
    "nombre": "Costa Rica",
    "bandera": "🇨🇷",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "83123456"
  },
  {
    "iso2": "HR",
    "prefijo": "+385",
    "nombre": "Croacia",
    "bandera": "🇭🇷",
    "minDigitos": 8,
    "maxDigitos": 9,
    "ejemplo": "911234567"
  },
  {
    "iso2": "CU",
    "prefijo": "+53",
    "nombre": "Cuba",
    "bandera": "🇨🇺",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "51234567"
  },
  {
    "iso2": "CW",
    "prefijo": "+599",
    "nombre": "Curazao",
    "bandera": "🇨🇼",
    "minDigitos": 7,
    "maxDigitos": 8,
    "ejemplo": "9512345"
  },
  {
    "iso2": "DK",
    "prefijo": "+45",
    "nombre": "Dinamarca",
    "bandera": "🇩🇰",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "20123456"
  },
  {
    "iso2": "DM",
    "prefijo": "+1767",
    "nombre": "Dominica",
    "bandera": "🇩🇲",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "2351234"
  },
  {
    "iso2": "EC",
    "prefijo": "+593",
    "nombre": "Ecuador",
    "bandera": "🇪🇨",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "991234567"
  },
  {
    "iso2": "EG",
    "prefijo": "+20",
    "nombre": "Egipto",
    "bandera": "🇪🇬",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "1001234567"
  },
  {
    "iso2": "SV",
    "prefijo": "+503",
    "nombre": "El Salvador",
    "bandera": "🇸🇻",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "70123456"
  },
  {
    "iso2": "AE",
    "prefijo": "+971",
    "nombre": "Emiratos Árabes Unidos",
    "bandera": "🇦🇪",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "501234567"
  },
  {
    "iso2": "ER",
    "prefijo": "+291",
    "nombre": "Eritrea",
    "bandera": "🇪🇷",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7123456"
  },
  {
    "iso2": "SK",
    "prefijo": "+421",
    "nombre": "Eslovaquia",
    "bandera": "🇸🇰",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "912123456"
  },
  {
    "iso2": "SI",
    "prefijo": "+386",
    "nombre": "Eslovenia",
    "bandera": "🇸🇮",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "41123456"
  },
  {
    "iso2": "ES",
    "prefijo": "+34",
    "nombre": "España",
    "bandera": "🇪🇸",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "612345678"
  },
  {
    "iso2": "US",
    "prefijo": "+1",
    "nombre": "Estados Unidos",
    "bandera": "🇺🇸",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "2025550123"
  },
  {
    "iso2": "EE",
    "prefijo": "+372",
    "nombre": "Estonia",
    "bandera": "🇪🇪",
    "minDigitos": 7,
    "maxDigitos": 8,
    "ejemplo": "5123456"
  },
  {
    "iso2": "SZ",
    "prefijo": "+268",
    "nombre": "Esuatini",
    "bandera": "🇸🇿",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "76123456"
  },
  {
    "iso2": "ET",
    "prefijo": "+251",
    "nombre": "Etiopía",
    "bandera": "🇪🇹",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "911234567"
  },
  {
    "iso2": "PH",
    "prefijo": "+63",
    "nombre": "Filipinas",
    "bandera": "🇵🇭",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "9171234567"
  },
  {
    "iso2": "FI",
    "prefijo": "+358",
    "nombre": "Finlandia",
    "bandera": "🇫🇮",
    "minDigitos": 9,
    "maxDigitos": 10,
    "ejemplo": "412345678"
  },
  {
    "iso2": "FJ",
    "prefijo": "+679",
    "nombre": "Fiyi",
    "bandera": "🇫🇯",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7012345"
  },
  {
    "iso2": "FR",
    "prefijo": "+33",
    "nombre": "Francia",
    "bandera": "🇫🇷",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "612345678"
  },
  {
    "iso2": "GA",
    "prefijo": "+241",
    "nombre": "Gabón",
    "bandera": "🇬🇦",
    "minDigitos": 7,
    "maxDigitos": 8,
    "ejemplo": "06123456"
  },
  {
    "iso2": "GM",
    "prefijo": "+220",
    "nombre": "Gambia",
    "bandera": "🇬🇲",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7012345"
  },
  {
    "iso2": "GE",
    "prefijo": "+995",
    "nombre": "Georgia",
    "bandera": "🇬🇪",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "555123456"
  },
  {
    "iso2": "GH",
    "prefijo": "+233",
    "nombre": "Ghana",
    "bandera": "🇬🇭",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "201234567"
  },
  {
    "iso2": "GI",
    "prefijo": "+350",
    "nombre": "Gibraltar",
    "bandera": "🇬🇮",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "57123456"
  },
  {
    "iso2": "GD",
    "prefijo": "+1473",
    "nombre": "Granada",
    "bandera": "🇬🇩",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "4031234"
  },
  {
    "iso2": "GR",
    "prefijo": "+30",
    "nombre": "Grecia",
    "bandera": "🇬🇷",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "6912345678"
  },
  {
    "iso2": "GL",
    "prefijo": "+299",
    "nombre": "Groenlandia",
    "bandera": "🇬🇱",
    "minDigitos": 6,
    "maxDigitos": 6,
    "ejemplo": "221234"
  },
  {
    "iso2": "GP",
    "prefijo": "+590",
    "nombre": "Guadalupe",
    "bandera": "🇬🇵",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "690123456"
  },
  {
    "iso2": "GU",
    "prefijo": "+1671",
    "nombre": "Guam",
    "bandera": "🇬🇺",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7271234"
  },
  {
    "iso2": "GT",
    "prefijo": "+502",
    "nombre": "Guatemala",
    "bandera": "🇬🇹",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "51234567"
  },
  {
    "iso2": "GF",
    "prefijo": "+594",
    "nombre": "Guayana Francesa",
    "bandera": "🇬🇫",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "694123456"
  },
  {
    "iso2": "GG",
    "prefijo": "+44",
    "nombre": "Guernsey",
    "bandera": "🇬🇬",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "7781123456"
  },
  {
    "iso2": "GN",
    "prefijo": "+224",
    "nombre": "Guinea",
    "bandera": "🇬🇳",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "621123456"
  },
  {
    "iso2": "GQ",
    "prefijo": "+240",
    "nombre": "Guinea Ecuatorial",
    "bandera": "🇬🇶",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "222123456"
  },
  {
    "iso2": "GW",
    "prefijo": "+245",
    "nombre": "Guinea-Bisáu",
    "bandera": "🇬🇼",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "9551234"
  },
  {
    "iso2": "GY",
    "prefijo": "+592",
    "nombre": "Guyana",
    "bandera": "🇬🇾",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "6091234"
  },
  {
    "iso2": "HT",
    "prefijo": "+509",
    "nombre": "Haití",
    "bandera": "🇭🇹",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "34123456"
  },
  {
    "iso2": "HN",
    "prefijo": "+504",
    "nombre": "Honduras",
    "bandera": "🇭🇳",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "91234567"
  },
  {
    "iso2": "HK",
    "prefijo": "+852",
    "nombre": "Hong Kong",
    "bandera": "🇭🇰",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "51234567"
  },
  {
    "iso2": "HU",
    "prefijo": "+36",
    "nombre": "Hungría",
    "bandera": "🇭🇺",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "201234567"
  },
  {
    "iso2": "IN",
    "prefijo": "+91",
    "nombre": "India",
    "bandera": "🇮🇳",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "9812345678"
  },
  {
    "iso2": "ID",
    "prefijo": "+62",
    "nombre": "Indonesia",
    "bandera": "🇮🇩",
    "minDigitos": 9,
    "maxDigitos": 12,
    "ejemplo": "8123456789"
  },
  {
    "iso2": "IQ",
    "prefijo": "+964",
    "nombre": "Irak",
    "bandera": "🇮🇶",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "7901234567"
  },
  {
    "iso2": "IR",
    "prefijo": "+98",
    "nombre": "Irán",
    "bandera": "🇮🇷",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "9121234567"
  },
  {
    "iso2": "IE",
    "prefijo": "+353",
    "nombre": "Irlanda",
    "bandera": "🇮🇪",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "851234567"
  },
  {
    "iso2": "IS",
    "prefijo": "+354",
    "nombre": "Islandia",
    "bandera": "🇮🇸",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "6123456"
  },
  {
    "iso2": "KY",
    "prefijo": "+1345",
    "nombre": "Islas Caimán",
    "bandera": "🇰🇾",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "9161234"
  },
  {
    "iso2": "CK",
    "prefijo": "+682",
    "nombre": "Islas Cook",
    "bandera": "🇨🇰",
    "minDigitos": 5,
    "maxDigitos": 5,
    "ejemplo": "51234"
  },
  {
    "iso2": "FO",
    "prefijo": "+298",
    "nombre": "Islas Feroe",
    "bandera": "🇫🇴",
    "minDigitos": 6,
    "maxDigitos": 6,
    "ejemplo": "211234"
  },
  {
    "iso2": "FK",
    "prefijo": "+500",
    "nombre": "Islas Malvinas",
    "bandera": "🇫🇰",
    "minDigitos": 5,
    "maxDigitos": 5,
    "ejemplo": "51234"
  },
  {
    "iso2": "MP",
    "prefijo": "+1670",
    "nombre": "Islas Marianas del Norte",
    "bandera": "🇲🇵",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "2871234"
  },
  {
    "iso2": "MH",
    "prefijo": "+692",
    "nombre": "Islas Marshall",
    "bandera": "🇲🇭",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "2351234"
  },
  {
    "iso2": "SB",
    "prefijo": "+677",
    "nombre": "Islas Salomón",
    "bandera": "🇸🇧",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7421234"
  },
  {
    "iso2": "TC",
    "prefijo": "+1649",
    "nombre": "Islas Turcas y Caicos",
    "bandera": "🇹🇨",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "2311234"
  },
  {
    "iso2": "VG",
    "prefijo": "+1284",
    "nombre": "Islas Vírgenes Británicas",
    "bandera": "🇻🇬",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "4411234"
  },
  {
    "iso2": "VI",
    "prefijo": "+1340",
    "nombre": "Islas Vírgenes de EE.UU.",
    "bandera": "🇻🇮",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "6901234"
  },
  {
    "iso2": "IL",
    "prefijo": "+972",
    "nombre": "Israel",
    "bandera": "🇮🇱",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "501234567"
  },
  {
    "iso2": "IT",
    "prefijo": "+39",
    "nombre": "Italia",
    "bandera": "🇮🇹",
    "minDigitos": 9,
    "maxDigitos": 10,
    "ejemplo": "3123456789"
  },
  {
    "iso2": "JM",
    "prefijo": "+1876",
    "nombre": "Jamaica",
    "bandera": "🇯🇲",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "3011234"
  },
  {
    "iso2": "JP",
    "prefijo": "+81",
    "nombre": "Japón",
    "bandera": "🇯🇵",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "9012345678"
  },
  {
    "iso2": "JE",
    "prefijo": "+44",
    "nombre": "Jersey",
    "bandera": "🇯🇪",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "7797123456"
  },
  {
    "iso2": "JO",
    "prefijo": "+962",
    "nombre": "Jordania",
    "bandera": "🇯🇴",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "790123456"
  },
  {
    "iso2": "KZ",
    "prefijo": "+7",
    "nombre": "Kazajistán",
    "bandera": "🇰🇿",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "7011234567"
  },
  {
    "iso2": "KE",
    "prefijo": "+254",
    "nombre": "Kenia",
    "bandera": "🇰🇪",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "712123456"
  },
  {
    "iso2": "KG",
    "prefijo": "+996",
    "nombre": "Kirguistán",
    "bandera": "🇰🇬",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "700123456"
  },
  {
    "iso2": "KI",
    "prefijo": "+686",
    "nombre": "Kiribati",
    "bandera": "🇰🇮",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "72012345"
  },
  {
    "iso2": "XK",
    "prefijo": "+383",
    "nombre": "Kosovo",
    "bandera": "🇽🇰",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "44123456"
  },
  {
    "iso2": "KW",
    "prefijo": "+965",
    "nombre": "Kuwait",
    "bandera": "🇰🇼",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "90012345"
  },
  {
    "iso2": "LA",
    "prefijo": "+856",
    "nombre": "Laos",
    "bandera": "🇱🇦",
    "minDigitos": 9,
    "maxDigitos": 10,
    "ejemplo": "2023123456"
  },
  {
    "iso2": "LS",
    "prefijo": "+266",
    "nombre": "Lesoto",
    "bandera": "🇱🇸",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "58012345"
  },
  {
    "iso2": "LV",
    "prefijo": "+371",
    "nombre": "Letonia",
    "bandera": "🇱🇻",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "21234567"
  },
  {
    "iso2": "LB",
    "prefijo": "+961",
    "nombre": "Líbano",
    "bandera": "🇱🇧",
    "minDigitos": 7,
    "maxDigitos": 8,
    "ejemplo": "71123456"
  },
  {
    "iso2": "LR",
    "prefijo": "+231",
    "nombre": "Liberia",
    "bandera": "🇱🇷",
    "minDigitos": 7,
    "maxDigitos": 8,
    "ejemplo": "77012345"
  },
  {
    "iso2": "LY",
    "prefijo": "+218",
    "nombre": "Libia",
    "bandera": "🇱🇾",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "912345678"
  },
  {
    "iso2": "LI",
    "prefijo": "+423",
    "nombre": "Liechtenstein",
    "bandera": "🇱🇮",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "6601234"
  },
  {
    "iso2": "LT",
    "prefijo": "+370",
    "nombre": "Lituania",
    "bandera": "🇱🇹",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "61234567"
  },
  {
    "iso2": "LU",
    "prefijo": "+352",
    "nombre": "Luxemburgo",
    "bandera": "🇱🇺",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "621123456"
  },
  {
    "iso2": "MO",
    "prefijo": "+853",
    "nombre": "Macao",
    "bandera": "🇲🇴",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "66123456"
  },
  {
    "iso2": "MK",
    "prefijo": "+389",
    "nombre": "Macedonia del Norte",
    "bandera": "🇲🇰",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "70123456"
  },
  {
    "iso2": "MG",
    "prefijo": "+261",
    "nombre": "Madagascar",
    "bandera": "🇲🇬",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "321234567"
  },
  {
    "iso2": "MY",
    "prefijo": "+60",
    "nombre": "Malasia",
    "bandera": "🇲🇾",
    "minDigitos": 9,
    "maxDigitos": 10,
    "ejemplo": "123456789"
  },
  {
    "iso2": "MW",
    "prefijo": "+265",
    "nombre": "Malaui",
    "bandera": "🇲🇼",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "991234567"
  },
  {
    "iso2": "MV",
    "prefijo": "+960",
    "nombre": "Maldivas",
    "bandera": "🇲🇻",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7712345"
  },
  {
    "iso2": "ML",
    "prefijo": "+223",
    "nombre": "Malí",
    "bandera": "🇲🇱",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "65012345"
  },
  {
    "iso2": "MT",
    "prefijo": "+356",
    "nombre": "Malta",
    "bandera": "🇲🇹",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "99123456"
  },
  {
    "iso2": "MA",
    "prefijo": "+212",
    "nombre": "Marruecos",
    "bandera": "🇲🇦",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "650123456"
  },
  {
    "iso2": "MQ",
    "prefijo": "+596",
    "nombre": "Martinica",
    "bandera": "🇲🇶",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "696123456"
  },
  {
    "iso2": "MU",
    "prefijo": "+230",
    "nombre": "Mauricio",
    "bandera": "🇲🇺",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "52512345"
  },
  {
    "iso2": "MR",
    "prefijo": "+222",
    "nombre": "Mauritania",
    "bandera": "🇲🇷",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "22123456"
  },
  {
    "iso2": "YT",
    "prefijo": "+262",
    "nombre": "Mayotte",
    "bandera": "🇾🇹",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "639123456"
  },
  {
    "iso2": "MX",
    "prefijo": "+52",
    "nombre": "México",
    "bandera": "🇲🇽",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "5512345678"
  },
  {
    "iso2": "FM",
    "prefijo": "+691",
    "nombre": "Micronesia",
    "bandera": "🇫🇲",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "9201234"
  },
  {
    "iso2": "MD",
    "prefijo": "+373",
    "nombre": "Moldavia",
    "bandera": "🇲🇩",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "68123456"
  },
  {
    "iso2": "MC",
    "prefijo": "+377",
    "nombre": "Mónaco",
    "bandera": "🇲🇨",
    "minDigitos": 8,
    "maxDigitos": 9,
    "ejemplo": "612345678"
  },
  {
    "iso2": "MN",
    "prefijo": "+976",
    "nombre": "Mongolia",
    "bandera": "🇲🇳",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "88123456"
  },
  {
    "iso2": "ME",
    "prefijo": "+382",
    "nombre": "Montenegro",
    "bandera": "🇲🇪",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "67123456"
  },
  {
    "iso2": "MS",
    "prefijo": "+1664",
    "nombre": "Montserrat",
    "bandera": "🇲🇸",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "4911234"
  },
  {
    "iso2": "MZ",
    "prefijo": "+258",
    "nombre": "Mozambique",
    "bandera": "🇲🇿",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "841234567"
  },
  {
    "iso2": "MM",
    "prefijo": "+95",
    "nombre": "Myanmar",
    "bandera": "🇲🇲",
    "minDigitos": 8,
    "maxDigitos": 10,
    "ejemplo": "9251234567"
  },
  {
    "iso2": "NA",
    "prefijo": "+264",
    "nombre": "Namibia",
    "bandera": "🇳🇦",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "811234567"
  },
  {
    "iso2": "NR",
    "prefijo": "+674",
    "nombre": "Nauru",
    "bandera": "🇳🇷",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "5551234"
  },
  {
    "iso2": "NP",
    "prefijo": "+977",
    "nombre": "Nepal",
    "bandera": "🇳🇵",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "9841234567"
  },
  {
    "iso2": "NI",
    "prefijo": "+505",
    "nombre": "Nicaragua",
    "bandera": "🇳🇮",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "81234567"
  },
  {
    "iso2": "NE",
    "prefijo": "+227",
    "nombre": "Níger",
    "bandera": "🇳🇪",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "96123456"
  },
  {
    "iso2": "NG",
    "prefijo": "+234",
    "nombre": "Nigeria",
    "bandera": "🇳🇬",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "8031234567"
  },
  {
    "iso2": "NU",
    "prefijo": "+683",
    "nombre": "Niue",
    "bandera": "🇳🇺",
    "minDigitos": 4,
    "maxDigitos": 4,
    "ejemplo": "4123"
  },
  {
    "iso2": "NO",
    "prefijo": "+47",
    "nombre": "Noruega",
    "bandera": "🇳🇴",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "41234567"
  },
  {
    "iso2": "NC",
    "prefijo": "+687",
    "nombre": "Nueva Caledonia",
    "bandera": "🇳🇨",
    "minDigitos": 6,
    "maxDigitos": 6,
    "ejemplo": "751234"
  },
  {
    "iso2": "NZ",
    "prefijo": "+64",
    "nombre": "Nueva Zelanda",
    "bandera": "🇳🇿",
    "minDigitos": 8,
    "maxDigitos": 10,
    "ejemplo": "211234567"
  },
  {
    "iso2": "OM",
    "prefijo": "+968",
    "nombre": "Omán",
    "bandera": "🇴🇲",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "91234567"
  },
  {
    "iso2": "NL",
    "prefijo": "+31",
    "nombre": "Países Bajos",
    "bandera": "🇳🇱",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "612345678"
  },
  {
    "iso2": "PK",
    "prefijo": "+92",
    "nombre": "Pakistán",
    "bandera": "🇵🇰",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "3001234567"
  },
  {
    "iso2": "PW",
    "prefijo": "+680",
    "nombre": "Palaos",
    "bandera": "🇵🇼",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7751234"
  },
  {
    "iso2": "PS",
    "prefijo": "+970",
    "nombre": "Palestina",
    "bandera": "🇵🇸",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "599123456"
  },
  {
    "iso2": "PA",
    "prefijo": "+507",
    "nombre": "Panamá",
    "bandera": "🇵🇦",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "61234567"
  },
  {
    "iso2": "PG",
    "prefijo": "+675",
    "nombre": "Papúa Nueva Guinea",
    "bandera": "🇵🇬",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "70123456"
  },
  {
    "iso2": "PY",
    "prefijo": "+595",
    "nombre": "Paraguay",
    "bandera": "🇵🇾",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "981123456"
  },
  {
    "iso2": "PE",
    "prefijo": "+51",
    "nombre": "Perú",
    "bandera": "🇵🇪",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "912345678"
  },
  {
    "iso2": "PF",
    "prefijo": "+689",
    "nombre": "Polinesia Francesa",
    "bandera": "🇵🇫",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "87123456"
  },
  {
    "iso2": "PL",
    "prefijo": "+48",
    "nombre": "Polonia",
    "bandera": "🇵🇱",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "512345678"
  },
  {
    "iso2": "PT",
    "prefijo": "+351",
    "nombre": "Portugal",
    "bandera": "🇵🇹",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "912345678"
  },
  {
    "iso2": "PR",
    "prefijo": "+1787",
    "nombre": "Puerto Rico",
    "bandera": "🇵🇷",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7871234"
  },
  {
    "iso2": "GB",
    "prefijo": "+44",
    "nombre": "Reino Unido",
    "bandera": "🇬🇧",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "7911123456"
  },
  {
    "iso2": "CF",
    "prefijo": "+236",
    "nombre": "República Centroafricana",
    "bandera": "🇨🇫",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "70012345"
  },
  {
    "iso2": "CZ",
    "prefijo": "+420",
    "nombre": "República Checa",
    "bandera": "🇨🇿",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "601123456"
  },
  {
    "iso2": "CG",
    "prefijo": "+242",
    "nombre": "República del Congo",
    "bandera": "🇨🇬",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "061234567"
  },
  {
    "iso2": "CD",
    "prefijo": "+243",
    "nombre": "República Democrática del Congo",
    "bandera": "🇨🇩",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "811234567"
  },
  {
    "iso2": "DO",
    "prefijo": "+1809",
    "nombre": "República Dominicana",
    "bandera": "🇩🇴",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "8091234"
  },
  {
    "iso2": "RE",
    "prefijo": "+262",
    "nombre": "Reunión",
    "bandera": "🇷🇪",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "692123456"
  },
  {
    "iso2": "RW",
    "prefijo": "+250",
    "nombre": "Ruanda",
    "bandera": "🇷🇼",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "788123456"
  },
  {
    "iso2": "RO",
    "prefijo": "+40",
    "nombre": "Rumania",
    "bandera": "🇷🇴",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "712345678"
  },
  {
    "iso2": "RU",
    "prefijo": "+7",
    "nombre": "Rusia",
    "bandera": "🇷🇺",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "9121234567"
  },
  {
    "iso2": "WS",
    "prefijo": "+685",
    "nombre": "Samoa",
    "bandera": "🇼🇸",
    "minDigitos": 5,
    "maxDigitos": 7,
    "ejemplo": "7212345"
  },
  {
    "iso2": "AS",
    "prefijo": "+1684",
    "nombre": "Samoa Americana",
    "bandera": "🇦🇸",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7331234"
  },
  {
    "iso2": "BL",
    "prefijo": "+590",
    "nombre": "San Bartolomé",
    "bandera": "🇧🇱",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "690123456"
  },
  {
    "iso2": "KN",
    "prefijo": "+1869",
    "nombre": "San Cristóbal y Nieves",
    "bandera": "🇰🇳",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "4651234"
  },
  {
    "iso2": "SM",
    "prefijo": "+378",
    "nombre": "San Marino",
    "bandera": "🇸🇲",
    "minDigitos": 8,
    "maxDigitos": 10,
    "ejemplo": "66123456"
  },
  {
    "iso2": "MF",
    "prefijo": "+590",
    "nombre": "San Martín",
    "bandera": "🇲🇫",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "690123456"
  },
  {
    "iso2": "PM",
    "prefijo": "+508",
    "nombre": "San Pedro y Miquelón",
    "bandera": "🇵🇲",
    "minDigitos": 6,
    "maxDigitos": 6,
    "ejemplo": "551234"
  },
  {
    "iso2": "VC",
    "prefijo": "+1784",
    "nombre": "San Vicente y las Granadinas",
    "bandera": "🇻🇨",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "4561234"
  },
  {
    "iso2": "SH",
    "prefijo": "+290",
    "nombre": "Santa Elena",
    "bandera": "🇸🇭",
    "minDigitos": 4,
    "maxDigitos": 5,
    "ejemplo": "51234"
  },
  {
    "iso2": "LC",
    "prefijo": "+1758",
    "nombre": "Santa Lucía",
    "bandera": "🇱🇨",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "4561234"
  },
  {
    "iso2": "ST",
    "prefijo": "+239",
    "nombre": "Santo Tomé y Príncipe",
    "bandera": "🇸🇹",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "9912345"
  },
  {
    "iso2": "SN",
    "prefijo": "+221",
    "nombre": "Senegal",
    "bandera": "🇸🇳",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "771234567"
  },
  {
    "iso2": "RS",
    "prefijo": "+381",
    "nombre": "Serbia",
    "bandera": "🇷🇸",
    "minDigitos": 8,
    "maxDigitos": 9,
    "ejemplo": "601234567"
  },
  {
    "iso2": "SC",
    "prefijo": "+248",
    "nombre": "Seychelles",
    "bandera": "🇸🇨",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "2512345"
  },
  {
    "iso2": "SL",
    "prefijo": "+232",
    "nombre": "Sierra Leona",
    "bandera": "🇸🇱",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "76123456"
  },
  {
    "iso2": "SG",
    "prefijo": "+65",
    "nombre": "Singapur",
    "bandera": "🇸🇬",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "81234567"
  },
  {
    "iso2": "SX",
    "prefijo": "+1721",
    "nombre": "Sint Maarten",
    "bandera": "🇸🇽",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "5201234"
  },
  {
    "iso2": "SY",
    "prefijo": "+963",
    "nombre": "Siria",
    "bandera": "🇸🇾",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "933123456"
  },
  {
    "iso2": "SO",
    "prefijo": "+252",
    "nombre": "Somalia",
    "bandera": "🇸🇴",
    "minDigitos": 8,
    "maxDigitos": 9,
    "ejemplo": "611234567"
  },
  {
    "iso2": "LK",
    "prefijo": "+94",
    "nombre": "Sri Lanka",
    "bandera": "🇱🇰",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "711234567"
  },
  {
    "iso2": "ZA",
    "prefijo": "+27",
    "nombre": "Sudáfrica",
    "bandera": "🇿🇦",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "711234567"
  },
  {
    "iso2": "SD",
    "prefijo": "+249",
    "nombre": "Sudán",
    "bandera": "🇸🇩",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "911234567"
  },
  {
    "iso2": "SS",
    "prefijo": "+211",
    "nombre": "Sudán del Sur",
    "bandera": "🇸🇸",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "977123456"
  },
  {
    "iso2": "SE",
    "prefijo": "+46",
    "nombre": "Suecia",
    "bandera": "🇸🇪",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "701234567"
  },
  {
    "iso2": "CH",
    "prefijo": "+41",
    "nombre": "Suiza",
    "bandera": "🇨🇭",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "791234567"
  },
  {
    "iso2": "SR",
    "prefijo": "+597",
    "nombre": "Surinam",
    "bandera": "🇸🇷",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "7412345"
  },
  {
    "iso2": "TH",
    "prefijo": "+66",
    "nombre": "Tailandia",
    "bandera": "🇹🇭",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "812345678"
  },
  {
    "iso2": "TW",
    "prefijo": "+886",
    "nombre": "Taiwán",
    "bandera": "🇹🇼",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "912345678"
  },
  {
    "iso2": "TZ",
    "prefijo": "+255",
    "nombre": "Tanzania",
    "bandera": "🇹🇿",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "712345678"
  },
  {
    "iso2": "TJ",
    "prefijo": "+992",
    "nombre": "Tayikistán",
    "bandera": "🇹🇯",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "918123456"
  },
  {
    "iso2": "TL",
    "prefijo": "+670",
    "nombre": "Timor Oriental",
    "bandera": "🇹🇱",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "77123456"
  },
  {
    "iso2": "TG",
    "prefijo": "+228",
    "nombre": "Togo",
    "bandera": "🇹🇬",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "90123456"
  },
  {
    "iso2": "TK",
    "prefijo": "+690",
    "nombre": "Tokelau",
    "bandera": "🇹🇰",
    "minDigitos": 4,
    "maxDigitos": 4,
    "ejemplo": "3123"
  },
  {
    "iso2": "TO",
    "prefijo": "+676",
    "nombre": "Tonga",
    "bandera": "🇹🇴",
    "minDigitos": 5,
    "maxDigitos": 7,
    "ejemplo": "7012345"
  },
  {
    "iso2": "TT",
    "prefijo": "+1868",
    "nombre": "Trinidad y Tobago",
    "bandera": "🇹🇹",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "2901234"
  },
  {
    "iso2": "TN",
    "prefijo": "+216",
    "nombre": "Túnez",
    "bandera": "🇹🇳",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "20123456"
  },
  {
    "iso2": "TM",
    "prefijo": "+993",
    "nombre": "Turkmenistán",
    "bandera": "🇹🇲",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "65123456"
  },
  {
    "iso2": "TR",
    "prefijo": "+90",
    "nombre": "Turquía",
    "bandera": "🇹🇷",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "5012345678"
  },
  {
    "iso2": "TV",
    "prefijo": "+688",
    "nombre": "Tuvalu",
    "bandera": "🇹🇻",
    "minDigitos": 5,
    "maxDigitos": 6,
    "ejemplo": "901234"
  },
  {
    "iso2": "UA",
    "prefijo": "+380",
    "nombre": "Ucrania",
    "bandera": "🇺🇦",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "501234567"
  },
  {
    "iso2": "UG",
    "prefijo": "+256",
    "nombre": "Uganda",
    "bandera": "🇺🇬",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "701234567"
  },
  {
    "iso2": "UY",
    "prefijo": "+598",
    "nombre": "Uruguay",
    "bandera": "🇺🇾",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "99123456"
  },
  {
    "iso2": "UZ",
    "prefijo": "+998",
    "nombre": "Uzbekistán",
    "bandera": "🇺🇿",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "901234567"
  },
  {
    "iso2": "VU",
    "prefijo": "+678",
    "nombre": "Vanuatu",
    "bandera": "🇻🇺",
    "minDigitos": 7,
    "maxDigitos": 7,
    "ejemplo": "5512345"
  },
  {
    "iso2": "VA",
    "prefijo": "+379",
    "nombre": "Vaticano",
    "bandera": "🇻🇦",
    "minDigitos": 8,
    "maxDigitos": 10,
    "ejemplo": "66981234"
  },
  {
    "iso2": "VE",
    "prefijo": "+58",
    "nombre": "Venezuela",
    "bandera": "🇻🇪",
    "minDigitos": 10,
    "maxDigitos": 10,
    "ejemplo": "4121234567"
  },
  {
    "iso2": "VN",
    "prefijo": "+84",
    "nombre": "Vietnam",
    "bandera": "🇻🇳",
    "minDigitos": 9,
    "maxDigitos": 10,
    "ejemplo": "912345678"
  },
  {
    "iso2": "WF",
    "prefijo": "+681",
    "nombre": "Wallis y Futuna",
    "bandera": "🇼🇫",
    "minDigitos": 6,
    "maxDigitos": 6,
    "ejemplo": "721234"
  },
  {
    "iso2": "YE",
    "prefijo": "+967",
    "nombre": "Yemen",
    "bandera": "🇾🇪",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "711234567"
  },
  {
    "iso2": "DJ",
    "prefijo": "+253",
    "nombre": "Yibuti",
    "bandera": "🇩🇯",
    "minDigitos": 8,
    "maxDigitos": 8,
    "ejemplo": "77123456"
  },
  {
    "iso2": "ZM",
    "prefijo": "+260",
    "nombre": "Zambia",
    "bandera": "🇿🇲",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "971234567"
  },
  {
    "iso2": "ZW",
    "prefijo": "+263",
    "nombre": "Zimbabue",
    "bandera": "🇿🇼",
    "minDigitos": 9,
    "maxDigitos": 9,
    "ejemplo": "711234567"
  }
];
