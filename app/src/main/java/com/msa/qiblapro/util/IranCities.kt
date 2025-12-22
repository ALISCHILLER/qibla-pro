package com.msa.qiblapro.util

data class IranCity(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val provinceFa: String,
    val lat: Double,
    val lon: Double
)

object IranCities {
    val cities = listOf(
        IranCity("tehran", "تهران", "Tehran", "تهران", 35.6892, 51.3890),
        IranCity("mashhad", "مشهد", "Mashhad", "خراسان رضوی", 36.2605, 59.6168),
        IranCity("isfahan", "اصفهان", "Isfahan", "اصفهان", 32.6546, 51.6680),
        IranCity("shiraz", "شیراز", "Shiraz", "فارس", 29.5926, 52.5836),
        IranCity("tabriz", "تبریز", "Tabriz", "آذربایجان شرقی", 38.0962, 46.2738),
        IranCity("karaj", "کرج", "Karaj", "البرز", 35.8400, 50.9391),
        IranCity("qom", "قم", "Qom", "قم", 34.6416, 50.8746),
        IranCity("ahvaz", "اهواز", "Ahvaz", "خوزستان", 31.3183, 48.6706),
        IranCity("kermanshah", "کرمانشاه", "Kermanshah", "کرمانشاه", 34.3142, 47.0650),
        IranCity("urmia", "ارومیه", "Urmia", "آذربایجان غربی", 37.5527, 45.0759),
        IranCity("rasht", "رشت", "Rasht", "گیلان", 37.2808, 49.5831),
        IranCity("zahedan", "زاهدان", "Zahedan", "سیستان و بلوچستان", 29.4963, 60.8629),
        IranCity("hamadan", "همدان", "Hamadan", "همدان", 34.7981, 48.5146),
        IranCity("kerman", "کرمان", "Kerman", "کرمان", 30.2839, 57.0833),
        IranCity("yazd", "یزد", "Yazd", "یزد", 31.8974, 54.3569),
        IranCity("ardabil", "اردبیل", "Ardabil", "اردبیل", 38.2498, 48.2933),
        IranCity("bandar_abbas", "بندرعباس", "Bandar Abbas", "هرمزگان", 27.1833, 56.2667),
        IranCity("arak", "اراک", "Arak", "مرکزی", 34.0917, 49.6892),
        IranCity("qazvin", "قزوین", "Qazvin", "قزوین", 36.2667, 50.0000),
        IranCity("zanjan", "زنجان", "Zanjan", "زنجان", 36.6736, 48.4787),
        IranCity("sari", "ساری", "Sari", "مازندران", 36.5633, 53.0601),
        IranCity("gorgan", "گرگان", "Gorgan", "گلستان", 36.8389, 54.4322),
        IranCity("bushehr", "بوشهر", "Bushehr", "بوشهر", 28.9234, 50.8203),
        IranCity("khorramabad", "خرم‌آباد", "Khorramabad", "لرستان", 33.4871, 48.3538),
        IranCity("sanandaj", "سنندج", "Sanandaj", "کردستان", 35.3113, 46.9960),
        IranCity("ilam", "ایلام", "Ilam", "ایلام", 33.6374, 46.4227),
        IranCity("shahr_e_kord", "شهرکرد", "Shahr-e Kord", "چهارمحال و بختیاری", 32.3256, 50.8644),
        IranCity("yasuj", "یاسوج", "Yasuj", "کهگیلویه و بویراحمد", 30.6681, 51.5875),
        IranCity("semnan", "سمنان", "Semnan", "سمنان", 35.5769, 53.3951),
        IranCity("bojnurd", "بجنورد", "Bojnurd", "خراسان شمالی", 37.4761, 57.3317),
        IranCity("birjand", "بیرجند", "Birjand", "خراسان جنوبی", 32.8653, 59.2164),
        IranCity("kashan", "کاشان", "Kashan", "اصفهان", 33.9850, 51.4100),
        IranCity("dezful", "دزفول", "Dezful", "خوزستان", 32.3811, 48.4058),
        IranCity("abadan", "آبادان", "Abadan", "خوزستان", 30.3392, 48.3043),
        IranCity("amol", "آمل", "Amol", "مازندران", 36.4676, 52.3507),
        IranCity("babol", "بابل", "Babol", "مازندران", 36.5500, 52.6833),
        IranCity("nayshabur", "نیشابور", "Nayshabur", "خراسان رضوی", 36.2133, 58.7958),
        IranCity("sabzevar", "سبزوار", "Sabzevar", "خراسان رضوی", 36.2167, 57.6833),
        IranCity("saveh", "ساوه", "Saveh", "مرکزی", 35.0214, 50.3556),
        IranCity("khoy", "خوی", "Khoy", "آذربایجان غربی", 38.5503, 44.9519)
    )
}
