/* Prism 1.30.0 -- prismjs.com -- MIT, see prism.LICENSE beside this file.
   VENDORED, UNMODIFIED apart from this header. Do not edit: re-vendor with
   `npm pack prismjs` and concatenate the Components below IN THAT ORDER,
   each stripped of its trailing newline and joined with one. clike is
   required by java, javascript and kotlin; markup must precede javascript,
   whose inlined `<script>` support attaches only if markup is loaded first.
   Components: core clike markup gherkin java javascript json kotlin properties python sql.
   Languages: atom clike gherkin html java javascript js json kotlin kt kts markup mathml plain plaintext properties py python rss sql ssml svg text txt webmanifest xml.
   Every name in Prism.languages, checked by test_highlight_grammars.py. */
var _self="undefined"!=typeof window?window:"undefined"!=typeof WorkerGlobalScope&&self instanceof WorkerGlobalScope?self:{},Prism=function(e){var n=/(?:^|\s)lang(?:uage)?-([\w-]+)(?=\s|$)/i,t=0,r={},a={manual:e.Prism&&e.Prism.manual,disableWorkerMessageHandler:e.Prism&&e.Prism.disableWorkerMessageHandler,util:{encode:function e(n){return n instanceof i?new i(n.type,e(n.content),n.alias):Array.isArray(n)?n.map(e):n.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/\u00a0/g," ")},type:function(e){return Object.prototype.toString.call(e).slice(8,-1)},objId:function(e){return e.__id||Object.defineProperty(e,"__id",{value:++t}),e.__id},clone:function e(n,t){var r,i;switch(t=t||{},a.util.type(n)){case"Object":if(i=a.util.objId(n),t[i])return t[i];for(var l in r={},t[i]=r,n)n.hasOwnProperty(l)&&(r[l]=e(n[l],t));return r;case"Array":return i=a.util.objId(n),t[i]?t[i]:(r=[],t[i]=r,n.forEach((function(n,a){r[a]=e(n,t)})),r);default:return n}},getLanguage:function(e){for(;e;){var t=n.exec(e.className);if(t)return t[1].toLowerCase();e=e.parentElement}return"none"},setLanguage:function(e,t){e.className=e.className.replace(RegExp(n,"gi"),""),e.classList.add("language-"+t)},currentScript:function(){if("undefined"==typeof document)return null;if(document.currentScript&&"SCRIPT"===document.currentScript.tagName)return document.currentScript;try{throw new Error}catch(r){var e=(/at [^(\r\n]*\((.*):[^:]+:[^:]+\)$/i.exec(r.stack)||[])[1];if(e){var n=document.getElementsByTagName("script");for(var t in n)if(n[t].src==e)return n[t]}return null}},isActive:function(e,n,t){for(var r="no-"+n;e;){var a=e.classList;if(a.contains(n))return!0;if(a.contains(r))return!1;e=e.parentElement}return!!t}},languages:{plain:r,plaintext:r,text:r,txt:r,extend:function(e,n){var t=a.util.clone(a.languages[e]);for(var r in n)t[r]=n[r];return t},insertBefore:function(e,n,t,r){var i=(r=r||a.languages)[e],l={};for(var o in i)if(i.hasOwnProperty(o)){if(o==n)for(var s in t)t.hasOwnProperty(s)&&(l[s]=t[s]);t.hasOwnProperty(o)||(l[o]=i[o])}var u=r[e];return r[e]=l,a.languages.DFS(a.languages,(function(n,t){t===u&&n!=e&&(this[n]=l)})),l},DFS:function e(n,t,r,i){i=i||{};var l=a.util.objId;for(var o in n)if(n.hasOwnProperty(o)){t.call(n,o,n[o],r||o);var s=n[o],u=a.util.type(s);"Object"!==u||i[l(s)]?"Array"!==u||i[l(s)]||(i[l(s)]=!0,e(s,t,o,i)):(i[l(s)]=!0,e(s,t,null,i))}}},plugins:{},highlightAll:function(e,n){a.highlightAllUnder(document,e,n)},highlightAllUnder:function(e,n,t){var r={callback:t,container:e,selector:'code[class*="language-"], [class*="language-"] code, code[class*="lang-"], [class*="lang-"] code'};a.hooks.run("before-highlightall",r),r.elements=Array.prototype.slice.apply(r.container.querySelectorAll(r.selector)),a.hooks.run("before-all-elements-highlight",r);for(var i,l=0;i=r.elements[l++];)a.highlightElement(i,!0===n,r.callback)},highlightElement:function(n,t,r){var i=a.util.getLanguage(n),l=a.languages[i];a.util.setLanguage(n,i);var o=n.parentElement;o&&"pre"===o.nodeName.toLowerCase()&&a.util.setLanguage(o,i);var s={element:n,language:i,grammar:l,code:n.textContent};function u(e){s.highlightedCode=e,a.hooks.run("before-insert",s),s.element.innerHTML=s.highlightedCode,a.hooks.run("after-highlight",s),a.hooks.run("complete",s),r&&r.call(s.element)}if(a.hooks.run("before-sanity-check",s),(o=s.element.parentElement)&&"pre"===o.nodeName.toLowerCase()&&!o.hasAttribute("tabindex")&&o.setAttribute("tabindex","0"),!s.code)return a.hooks.run("complete",s),void(r&&r.call(s.element));if(a.hooks.run("before-highlight",s),s.grammar)if(t&&e.Worker){var c=new Worker(a.filename);c.onmessage=function(e){u(e.data)},c.postMessage(JSON.stringify({language:s.language,code:s.code,immediateClose:!0}))}else u(a.highlight(s.code,s.grammar,s.language));else u(a.util.encode(s.code))},highlight:function(e,n,t){var r={code:e,grammar:n,language:t};if(a.hooks.run("before-tokenize",r),!r.grammar)throw new Error('The language "'+r.language+'" has no grammar.');return r.tokens=a.tokenize(r.code,r.grammar),a.hooks.run("after-tokenize",r),i.stringify(a.util.encode(r.tokens),r.language)},tokenize:function(e,n){var t=n.rest;if(t){for(var r in t)n[r]=t[r];delete n.rest}var a=new s;return u(a,a.head,e),o(e,a,n,a.head,0),function(e){for(var n=[],t=e.head.next;t!==e.tail;)n.push(t.value),t=t.next;return n}(a)},hooks:{all:{},add:function(e,n){var t=a.hooks.all;t[e]=t[e]||[],t[e].push(n)},run:function(e,n){var t=a.hooks.all[e];if(t&&t.length)for(var r,i=0;r=t[i++];)r(n)}},Token:i};function i(e,n,t,r){this.type=e,this.content=n,this.alias=t,this.length=0|(r||"").length}function l(e,n,t,r){e.lastIndex=n;var a=e.exec(t);if(a&&r&&a[1]){var i=a[1].length;a.index+=i,a[0]=a[0].slice(i)}return a}function o(e,n,t,r,s,g){for(var f in t)if(t.hasOwnProperty(f)&&t[f]){var h=t[f];h=Array.isArray(h)?h:[h];for(var d=0;d<h.length;++d){if(g&&g.cause==f+","+d)return;var v=h[d],p=v.inside,m=!!v.lookbehind,y=!!v.greedy,k=v.alias;if(y&&!v.pattern.global){var x=v.pattern.toString().match(/[imsuy]*$/)[0];v.pattern=RegExp(v.pattern.source,x+"g")}for(var b=v.pattern||v,w=r.next,A=s;w!==n.tail&&!(g&&A>=g.reach);A+=w.value.length,w=w.next){var P=w.value;if(n.length>e.length)return;if(!(P instanceof i)){var E,S=1;if(y){if(!(E=l(b,A,e,m))||E.index>=e.length)break;var L=E.index,O=E.index+E[0].length,C=A;for(C+=w.value.length;L>=C;)C+=(w=w.next).value.length;if(A=C-=w.value.length,w.value instanceof i)continue;for(var j=w;j!==n.tail&&(C<O||"string"==typeof j.value);j=j.next)S++,C+=j.value.length;S--,P=e.slice(A,C),E.index-=A}else if(!(E=l(b,0,P,m)))continue;L=E.index;var N=E[0],_=P.slice(0,L),M=P.slice(L+N.length),W=A+P.length;g&&W>g.reach&&(g.reach=W);var I=w.prev;if(_&&(I=u(n,I,_),A+=_.length),c(n,I,S),w=u(n,I,new i(f,p?a.tokenize(N,p):N,k,N)),M&&u(n,w,M),S>1){var T={cause:f+","+d,reach:W};o(e,n,t,w.prev,A,T),g&&T.reach>g.reach&&(g.reach=T.reach)}}}}}}function s(){var e={value:null,prev:null,next:null},n={value:null,prev:e,next:null};e.next=n,this.head=e,this.tail=n,this.length=0}function u(e,n,t){var r=n.next,a={value:t,prev:n,next:r};return n.next=a,r.prev=a,e.length++,a}function c(e,n,t){for(var r=n.next,a=0;a<t&&r!==e.tail;a++)r=r.next;n.next=r,r.prev=n,e.length-=a}if(e.Prism=a,i.stringify=function e(n,t){if("string"==typeof n)return n;if(Array.isArray(n)){var r="";return n.forEach((function(n){r+=e(n,t)})),r}var i={type:n.type,content:e(n.content,t),tag:"span",classes:["token",n.type],attributes:{},language:t},l=n.alias;l&&(Array.isArray(l)?Array.prototype.push.apply(i.classes,l):i.classes.push(l)),a.hooks.run("wrap",i);var o="";for(var s in i.attributes)o+=" "+s+'="'+(i.attributes[s]||"").replace(/"/g,"&quot;")+'"';return"<"+i.tag+' class="'+i.classes.join(" ")+'"'+o+">"+i.content+"</"+i.tag+">"},!e.document)return e.addEventListener?(a.disableWorkerMessageHandler||e.addEventListener("message",(function(n){var t=JSON.parse(n.data),r=t.language,i=t.code,l=t.immediateClose;e.postMessage(a.highlight(i,a.languages[r],r)),l&&e.close()}),!1),a):a;var g=a.util.currentScript();function f(){a.manual||a.highlightAll()}if(g&&(a.filename=g.src,g.hasAttribute("data-manual")&&(a.manual=!0)),!a.manual){var h=document.readyState;"loading"===h||"interactive"===h&&g&&g.defer?document.addEventListener("DOMContentLoaded",f):window.requestAnimationFrame?window.requestAnimationFrame(f):window.setTimeout(f,16)}return a}(_self);"undefined"!=typeof module&&module.exports&&(module.exports=Prism),"undefined"!=typeof global&&(global.Prism=Prism);
Prism.languages.clike={comment:[{pattern:/(^|[^\\])\/\*[\s\S]*?(?:\*\/|$)/,lookbehind:!0,greedy:!0},{pattern:/(^|[^\\:])\/\/.*/,lookbehind:!0,greedy:!0}],string:{pattern:/(["'])(?:\\(?:\r\n|[\s\S])|(?!\1)[^\\\r\n])*\1/,greedy:!0},"class-name":{pattern:/(\b(?:class|extends|implements|instanceof|interface|new|trait)\s+|\bcatch\s+\()[\w.\\]+/i,lookbehind:!0,inside:{punctuation:/[.\\]/}},keyword:/\b(?:break|catch|continue|do|else|finally|for|function|if|in|instanceof|new|null|return|throw|try|while)\b/,boolean:/\b(?:false|true)\b/,function:/\b\w+(?=\()/,number:/\b0x[\da-f]+\b|(?:\b\d+(?:\.\d*)?|\B\.\d+)(?:e[+-]?\d+)?/i,operator:/[<>]=?|[!=]=?=?|--?|\+\+?|&&?|\|\|?|[?*/~^%]/,punctuation:/[{}[\];(),.:]/};
Prism.languages.markup={comment:{pattern:/<!--(?:(?!<!--)[\s\S])*?-->/,greedy:!0},prolog:{pattern:/<\?[\s\S]+?\?>/,greedy:!0},doctype:{pattern:/<!DOCTYPE(?:[^>"'[\]]|"[^"]*"|'[^']*')+(?:\[(?:[^<"'\]]|"[^"]*"|'[^']*'|<(?!!--)|<!--(?:[^-]|-(?!->))*-->)*\]\s*)?>/i,greedy:!0,inside:{"internal-subset":{pattern:/(^[^\[]*\[)[\s\S]+(?=\]>$)/,lookbehind:!0,greedy:!0,inside:null},string:{pattern:/"[^"]*"|'[^']*'/,greedy:!0},punctuation:/^<!|>$|[[\]]/,"doctype-tag":/^DOCTYPE/i,name:/[^\s<>'"]+/}},cdata:{pattern:/<!\[CDATA\[[\s\S]*?\]\]>/i,greedy:!0},tag:{pattern:/<\/?(?!\d)[^\s>\/=$<%]+(?:\s(?:\s*[^\s>\/=]+(?:\s*=\s*(?:"[^"]*"|'[^']*'|[^\s'">=]+(?=[\s>]))|(?=[\s/>])))+)?\s*\/?>/,greedy:!0,inside:{tag:{pattern:/^<\/?[^\s>\/]+/,inside:{punctuation:/^<\/?/,namespace:/^[^\s>\/:]+:/}},"special-attr":[],"attr-value":{pattern:/=\s*(?:"[^"]*"|'[^']*'|[^\s'">=]+)/,inside:{punctuation:[{pattern:/^=/,alias:"attr-equals"},{pattern:/^(\s*)["']|["']$/,lookbehind:!0}]}},punctuation:/\/?>/,"attr-name":{pattern:/[^\s>\/]+/,inside:{namespace:/^[^\s>\/:]+:/}}}},entity:[{pattern:/&[\da-z]{1,8};/i,alias:"named-entity"},/&#x?[\da-f]{1,8};/i]},Prism.languages.markup.tag.inside["attr-value"].inside.entity=Prism.languages.markup.entity,Prism.languages.markup.doctype.inside["internal-subset"].inside=Prism.languages.markup,Prism.hooks.add("wrap",(function(a){"entity"===a.type&&(a.attributes.title=a.content.replace(/&amp;/,"&"))})),Object.defineProperty(Prism.languages.markup.tag,"addInlined",{value:function(a,e){var s={};s["language-"+e]={pattern:/(^<!\[CDATA\[)[\s\S]+?(?=\]\]>$)/i,lookbehind:!0,inside:Prism.languages[e]},s.cdata=/^<!\[CDATA\[|\]\]>$/i;var t={"included-cdata":{pattern:/<!\[CDATA\[[\s\S]*?\]\]>/i,inside:s}};t["language-"+e]={pattern:/[\s\S]+/,inside:Prism.languages[e]};var n={};n[a]={pattern:RegExp("(<__[^>]*>)(?:<!\\[CDATA\\[(?:[^\\]]|\\](?!\\]>))*\\]\\]>|(?!<!\\[CDATA\\[)[^])*?(?=</__>)".replace(/__/g,(function(){return a})),"i"),lookbehind:!0,greedy:!0,inside:t},Prism.languages.insertBefore("markup","cdata",n)}}),Object.defineProperty(Prism.languages.markup.tag,"addAttribute",{value:function(a,e){Prism.languages.markup.tag.inside["special-attr"].push({pattern:RegExp("(^|[\"'\\s])(?:"+a+")\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s'\">=]+(?=[\\s>]))","i"),lookbehind:!0,inside:{"attr-name":/^[^\s=]+/,"attr-value":{pattern:/=[\s\S]+/,inside:{value:{pattern:/(^=\s*(["']|(?!["'])))\S[\s\S]*(?=\2$)/,lookbehind:!0,alias:[e,"language-"+e],inside:Prism.languages[e]},punctuation:[{pattern:/^=/,alias:"attr-equals"},/"|'/]}}}})}}),Prism.languages.html=Prism.languages.markup,Prism.languages.mathml=Prism.languages.markup,Prism.languages.svg=Prism.languages.markup,Prism.languages.xml=Prism.languages.extend("markup",{}),Prism.languages.ssml=Prism.languages.xml,Prism.languages.atom=Prism.languages.xml,Prism.languages.rss=Prism.languages.xml;
!function(a){var n="(?:\r?\n|\r)[ \t]*\\|.+\\|(?:(?!\\|).)*";a.languages.gherkin={pystring:{pattern:/("""|''')[\s\S]+?\1/,alias:"string"},comment:{pattern:/(^[ \t]*)#.*/m,lookbehind:!0},tag:{pattern:/(^[ \t]*)@\S*/m,lookbehind:!0},feature:{pattern:/((?:^|\r?\n|\r)[ \t]*)(?:Ability|Ahoy matey!|Arwedd|Aspekt|Besigheid Behoefte|Business Need|Caracteristica|Característica|Egenskab|Egenskap|Eiginleiki|Feature|Fīča|Fitur|Fonctionnalité|Fonksyonalite|Funcionalidade|Funcionalitat|Functionalitate|Funcţionalitate|Funcționalitate|Functionaliteit|Fungsi|Funkcia|Funkcija|Funkcionalitāte|Funkcionalnost|Funkcja|Funksie|Funktionalität|Funktionalitéit|Funzionalità|Hwaet|Hwæt|Jellemző|Karakteristik|Lastnost|Mak|Mogucnost|laH|Mogućnost|Moznosti|Možnosti|OH HAI|Omadus|Ominaisuus|Osobina|Özellik|Potrzeba biznesowa|perbogh|poQbogh malja'|Požadavek|Požiadavka|Pretty much|Qap|Qu'meH 'ut|Savybė|Tính năng|Trajto|Vermoë|Vlastnosť|Właściwość|Značilnost|Δυνατότητα|Λειτουργία|Могућност|Мөмкинлек|Особина|Свойство|Үзенчәлеклелек|Функционал|Функционалност|Функция|Функціонал|תכונה|خاصية|خصوصیت|صلاحیت|کاروبار کی ضرورت|وِیژگی|रूप लेख|ਖਾਸੀਅਤ|ਨਕਸ਼ ਨੁਹਾਰ|ਮੁਹਾਂਦਰਾ|గుణము|ಹೆಚ್ಚಳ|ความต้องการทางธุรกิจ|ความสามารถ|โครงหลัก|기능|フィーチャ|功能|機能):(?:[^:\r\n]+(?:\r?\n|\r|$))*/,lookbehind:!0,inside:{important:{pattern:/(:)[^\r\n]+/,lookbehind:!0},keyword:/[^:\r\n]+:/}},scenario:{pattern:/(^[ \t]*)(?:Abstract Scenario|Abstrakt Scenario|Achtergrond|Aer|Ær|Agtergrond|All y'all|Antecedentes|Antecedents|Atburðarás|Atburðarásir|Awww, look mate|B4|Background|Baggrund|Bakgrund|Bakgrunn|Bakgrunnur|Beispiele|Beispiller|Bối cảnh|Cefndir|Cenario|Cenário|Cenario de Fundo|Cenário de Fundo|Cenarios|Cenários|Contesto|Context|Contexte|Contexto|Conto|Contoh|Contone|Dæmi|Dasar|Dead men tell no tales|Delineacao do Cenario|Delineação do Cenário|Dis is what went down|Dữ liệu|Dyagram Senaryo|Dyagram senaryo|Egzanp|Ejemplos|Eksempler|Ekzemploj|Enghreifftiau|Esbozo do escenario|Escenari|Escenario|Esempi|Esquema de l'escenari|Esquema del escenario|Esquema do Cenario|Esquema do Cenário|EXAMPLZ|Examples|Exempel|Exemple|Exemples|Exemplos|First off|Fono|Forgatókönyv|Forgatókönyv vázlat|Fundo|Geçmiş|Grundlage|Hannergrond|ghantoH|Háttér|Heave to|Istorik|Juhtumid|Keadaan|Khung kịch bản|Khung tình huống|Kịch bản|Koncept|Konsep skenario|Kontèks|Kontekst|Kontekstas|Konteksts|Kontext|Konturo de la scenaro|Latar Belakang|lut chovnatlh|lut|lutmey|Lýsing Atburðarásar|Lýsing Dæma|MISHUN SRSLY|MISHUN|Menggariskan Senario|mo'|Náčrt Scenára|Náčrt Scénáře|Náčrt Scenáru|Oris scenarija|Örnekler|Osnova|Osnova Scenára|Osnova scénáře|Osnutek|Ozadje|Paraugs|Pavyzdžiai|Példák|Piemēri|Plan du scénario|Plan du Scénario|Plan Senaryo|Plan senaryo|Plang vum Szenario|Pozadí|Pozadie|Pozadina|Príklady|Příklady|Primer|Primeri|Primjeri|Przykłady|Raamstsenaarium|Reckon it's like|Rerefons|Scenár|Scénář|Scenarie|Scenarij|Scenarijai|Scenarijaus šablonas|Scenariji|Scenārijs|Scenārijs pēc parauga|Scenarijus|Scenario|Scénario|Scenario Amlinellol|Scenario Outline|Scenario Template|Scenariomal|Scenariomall|Scenarios|Scenariu|Scenariusz|Scenaro|Schema dello scenario|Se ðe|Se the|Se þe|Senario|Senaryo Deskripsyon|Senaryo deskripsyon|Senaryo|Senaryo taslağı|Shiver me timbers|Situācija|Situai|Situasie Uiteensetting|Situasie|Skenario konsep|Skenario|Skica|Structura scenariu|Structură scenariu|Struktura scenarija|Stsenaarium|Swa hwaer swa|Swa|Swa hwær swa|Szablon scenariusza|Szenario|Szenariogrundriss|Tapaukset|Tapaus|Tapausaihio|Taust|Tausta|Template Keadaan|Template Senario|Template Situai|The thing of it is|Tình huống|Variantai|Voorbeelde|Voorbeelden|Wharrimean is|Yo-ho-ho|You'll wanna|Założenia|Παραδείγματα|Περιγραφή Σεναρίου|Σενάρια|Σενάριο|Υπόβαθρο|Кереш|Контекст|Концепт|Мисаллар|Мисоллар|Основа|Передумова|Позадина|Предистория|Предыстория|Приклади|Пример|Примери|Примеры|Рамка на сценарий|Скица|Структура сценарија|Структура сценария|Структура сценарію|Сценарий|Сценарий структураси|Сценарийның төзелеше|Сценарији|Сценарио|Сценарій|Тарих|Үрнәкләр|דוגמאות|רקע|תבנית תרחיש|תרחיש|الخلفية|الگوی سناریو|امثلة|پس منظر|زمینه|سناریو|سيناريو|سيناريو مخطط|مثالیں|منظر نامے کا خاکہ|منظرنامہ|نمونه ها|उदाहरण|परिदृश्य|परिदृश्य रूपरेखा|पृष्ठभूमि|ਉਦਾਹਰਨਾਂ|ਪਟਕਥਾ|ਪਟਕਥਾ ਢਾਂਚਾ|ਪਟਕਥਾ ਰੂਪ ਰੇਖਾ|ਪਿਛੋਕੜ|ఉదాహరణలు|కథనం|నేపథ్యం|సన్నివేశం|ಉದಾಹರಣೆಗಳು|ಕಥಾಸಾರಾಂಶ|ವಿವರಣೆ|ಹಿನ್ನೆಲೆ|โครงสร้างของเหตุการณ์|ชุดของตัวอย่าง|ชุดของเหตุการณ์|แนวคิด|สรุปเหตุการณ์|เหตุการณ์|배경|시나리오|시나리오 개요|예|サンプル|シナリオ|シナリオアウトライン|シナリオテンプレ|シナリオテンプレート|テンプレ|例|例子|剧本|剧本大纲|劇本|劇本大綱|场景|场景大纲|場景|場景大綱|背景):[^:\r\n]*/m,lookbehind:!0,inside:{important:{pattern:/(:)[^\r\n]*/,lookbehind:!0},keyword:/[^:\r\n]+:/}},"table-body":{pattern:RegExp("("+n+")(?:"+n+")+"),lookbehind:!0,inside:{outline:{pattern:/<[^>]+>/,alias:"variable"},td:{pattern:/\s*[^\s|][^|]*/,alias:"string"},punctuation:/\|/}},"table-head":{pattern:RegExp(n),inside:{th:{pattern:/\s*[^\s|][^|]*/,alias:"variable"},punctuation:/\|/}},atrule:{pattern:/(^[ \t]+)(?:'a|'ach|'ej|7|a|A také|A taktiež|A tiež|A zároveň|Aber|Ac|Adott|Akkor|Ak|Aleshores|Ale|Ali|Allora|Alors|Als|Ama|Amennyiben|Amikor|Ampak|an|AN|Ananging|And y'all|And|Angenommen|Anrhegedig a|An|Apabila|Atès|Atesa|Atunci|Avast!|Aye|A|awer|Bagi|Banjur|Bet|Biết|Blimey!|Buh|But at the end of the day I reckon|But y'all|But|BUT|Cal|Când|Cand|Cando|Ce|Cuando|Če|Ða ðe|Ða|Dadas|Dada|Dados|Dado|DaH ghu' bejlu'|dann|Dann|Dano|Dan|Dar|Dat fiind|Data|Date fiind|Date|Dati fiind|Dati|Daţi fiind|Dați fiind|DEN|Dato|De|Den youse gotta|Dengan|Diberi|Diyelim ki|Donada|Donat|Donitaĵo|Do|Dun|Duota|Ðurh|Eeldades|Ef|Eğer ki|Entao|Então|Entón|E|En|Entonces|Epi|És|Etant donnée|Etant donné|Et|Étant données|Étant donnée|Étant donné|Etant données|Etant donnés|Étant donnés|Fakat|Gangway!|Gdy|Gegeben seien|Gegeben sei|Gegeven|Gegewe|ghu' noblu'|Gitt|Given y'all|Given|Givet|Givun|Ha|Cho|I CAN HAZ|In|Ir|It's just unbelievable|I|Ja|Jeśli|Jeżeli|Kad|Kada|Kadar|Kai|Kaj|Když|Keď|Kemudian|Ketika|Khi|Kiedy|Ko|Kuid|Kui|Kun|Lan|latlh|Le sa a|Let go and haul|Le|Lè sa a|Lè|Logo|Lorsqu'<|Lorsque|mä|Maar|Mais|Mając|Ma|Majd|Maka|Manawa|Mas|Men|Menawa|Mutta|Nalika|Nalikaning|Nanging|Når|När|Nato|Nhưng|Niin|Njuk|O zaman|Och|Og|Oletetaan|Ond|Onda|Oraz|Pak|Pero|Però|Podano|Pokiaľ|Pokud|Potem|Potom|Privzeto|Pryd|Quan|Quand|Quando|qaSDI'|Så|Sed|Se|Siis|Sipoze ke|Sipoze Ke|Sipoze|Si|Şi|Și|Soit|Stel|Tada|Tad|Takrat|Tak|Tapi|Ter|Tetapi|Tha the|Tha|Then y'all|Then|Thì|Thurh|Toda|Too right|Un|Und|ugeholl|Và|vaj|Vendar|Ve|wann|Wanneer|WEN|Wenn|When y'all|When|Wtedy|Wun|Y'know|Yeah nah|Yna|Youse know like when|Youse know when youse got|Y|Za predpokladu|Za předpokladu|Zadan|Zadani|Zadano|Zadate|Zadato|Zakładając|Zaradi|Zatati|Þa þe|Þa|Þá|Þegar|Þurh|Αλλά|Δεδομένου|Και|Όταν|Τότε|А також|Агар|Але|Али|Аммо|А|Әгәр|Әйтик|Әмма|Бирок|Ва|Вә|Дадено|Дано|Допустим|Если|Задате|Задати|Задато|И|І|К тому же|Када|Кад|Когато|Когда|Коли|Ләкин|Лекин|Нәтиҗәдә|Нехай|Но|Онда|Припустимо, що|Припустимо|Пусть|Также|Та|Тогда|Тоді|То|Унда|Һәм|Якщо|אבל|אזי|אז|בהינתן|וגם|כאשר|آنگاه|اذاً|اگر|اما|اور|با فرض|بالفرض|بفرض|پھر|تب|ثم|جب|عندما|فرض کیا|لكن|لیکن|متى|هنگامی|و|अगर|और|कदा|किन्तु|चूंकि|जब|तथा|तदा|तब|परन्तु|पर|यदि|ਅਤੇ|ਜਦੋਂ|ਜਿਵੇਂ ਕਿ|ਜੇਕਰ|ਤਦ|ਪਰ|అప్పుడు|ఈ పరిస్థితిలో|కాని|చెప్పబడినది|మరియు|ಆದರೆ|ನಂತರ|ನೀಡಿದ|ಮತ್ತು|ಸ್ಥಿತಿಯನ್ನು|กำหนดให้|ดังนั้น|แต่|เมื่อ|และ|그러면<|그리고<|단<|만약<|만일<|먼저<|조건<|하지만<|かつ<|しかし<|ただし<|ならば<|もし<|並且<|但し<|但是<|假如<|假定<|假設<|假设<|前提<|同时<|同時<|并且<|当<|當<|而且<|那么<|那麼<)(?=[ \t])/m,lookbehind:!0},string:{pattern:/"(?:\\.|[^"\\\r\n])*"|'(?:\\.|[^'\\\r\n])*'/,inside:{outline:{pattern:/<[^>]+>/,alias:"variable"}}},outline:{pattern:/<[^>]+>/,alias:"variable"}}}(Prism);
!function(e){var n=/\b(?:abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|exports|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|module|native|new|non-sealed|null|open|opens|package|permits|private|protected|provides|public|record(?!\s*[(){}[\]<>=%~.:,;?+\-*/&|^])|requires|return|sealed|short|static|strictfp|super|switch|synchronized|this|throw|throws|to|transient|transitive|try|uses|var|void|volatile|while|with|yield)\b/,t="(?:[a-z]\\w*\\s*\\.\\s*)*(?:[A-Z]\\w*\\s*\\.\\s*)*",s={pattern:RegExp("(^|[^\\w.])"+t+"[A-Z](?:[\\d_A-Z]*[a-z]\\w*)?\\b"),lookbehind:!0,inside:{namespace:{pattern:/^[a-z]\w*(?:\s*\.\s*[a-z]\w*)*(?:\s*\.)?/,inside:{punctuation:/\./}},punctuation:/\./}};e.languages.java=e.languages.extend("clike",{string:{pattern:/(^|[^\\])"(?:\\.|[^"\\\r\n])*"/,lookbehind:!0,greedy:!0},"class-name":[s,{pattern:RegExp("(^|[^\\w.])"+t+"[A-Z]\\w*(?=\\s+\\w+\\s*[;,=()]|\\s*(?:\\[[\\s,]*\\]\\s*)?::\\s*new\\b)"),lookbehind:!0,inside:s.inside},{pattern:RegExp("(\\b(?:class|enum|extends|implements|instanceof|interface|new|record|throws)\\s+)"+t+"[A-Z]\\w*\\b"),lookbehind:!0,inside:s.inside}],keyword:n,function:[e.languages.clike.function,{pattern:/(::\s*)[a-z_]\w*/,lookbehind:!0}],number:/\b0b[01][01_]*L?\b|\b0x(?:\.[\da-f_p+-]+|[\da-f_]+(?:\.[\da-f_p+-]+)?)\b|(?:\b\d[\d_]*(?:\.[\d_]*)?|\B\.\d[\d_]*)(?:e[+-]?\d[\d_]*)?[dfl]?/i,operator:{pattern:/(^|[^.])(?:<<=?|>>>?=?|->|--|\+\+|&&|\|\||::|[?:~]|[-+*/%&|^!=<>]=?)/m,lookbehind:!0},constant:/\b[A-Z][A-Z_\d]+\b/}),e.languages.insertBefore("java","string",{"triple-quoted-string":{pattern:/"""[ \t]*[\r\n](?:(?:"|"")?(?:\\.|[^"\\]))*"""/,greedy:!0,alias:"string"},char:{pattern:/'(?:\\.|[^'\\\r\n]){1,6}'/,greedy:!0}}),e.languages.insertBefore("java","class-name",{annotation:{pattern:/(^|[^.])@\w+(?:\s*\.\s*\w+)*/,lookbehind:!0,alias:"punctuation"},generics:{pattern:/<(?:[\w\s,.?]|&(?!&)|<(?:[\w\s,.?]|&(?!&)|<(?:[\w\s,.?]|&(?!&)|<(?:[\w\s,.?]|&(?!&))*>)*>)*>)*>/,inside:{"class-name":s,keyword:n,punctuation:/[<>(),.:]/,operator:/[?&|]/}},import:[{pattern:RegExp("(\\bimport\\s+)"+t+"(?:[A-Z]\\w*|\\*)(?=\\s*;)"),lookbehind:!0,inside:{namespace:s.inside.namespace,punctuation:/\./,operator:/\*/,"class-name":/\w+/}},{pattern:RegExp("(\\bimport\\s+static\\s+)"+t+"(?:\\w+|\\*)(?=\\s*;)"),lookbehind:!0,alias:"static",inside:{namespace:s.inside.namespace,static:/\b\w+$/,punctuation:/\./,operator:/\*/,"class-name":/\w+/}}],namespace:{pattern:RegExp("(\\b(?:exports|import(?:\\s+static)?|module|open|opens|package|provides|requires|to|transitive|uses|with)\\s+)(?!<keyword>)[a-z]\\w*(?:\\.[a-z]\\w*)*\\.?".replace(/<keyword>/g,(function(){return n.source}))),lookbehind:!0,inside:{punctuation:/\./}}})}(Prism);
Prism.languages.javascript=Prism.languages.extend("clike",{"class-name":[Prism.languages.clike["class-name"],{pattern:/(^|[^$\w\xA0-\uFFFF])(?!\s)[_$A-Z\xA0-\uFFFF](?:(?!\s)[$\w\xA0-\uFFFF])*(?=\.(?:constructor|prototype))/,lookbehind:!0}],keyword:[{pattern:/((?:^|\})\s*)catch\b/,lookbehind:!0},{pattern:/(^|[^.]|\.\.\.\s*)\b(?:as|assert(?=\s*\{)|async(?=\s*(?:function\b|\(|[$\w\xA0-\uFFFF]|$))|await|break|case|class|const|continue|debugger|default|delete|do|else|enum|export|extends|finally(?=\s*(?:\{|$))|for|from(?=\s*(?:['"]|$))|function|(?:get|set)(?=\s*(?:[#\[$\w\xA0-\uFFFF]|$))|if|implements|import|in|instanceof|interface|let|new|null|of|package|private|protected|public|return|static|super|switch|this|throw|try|typeof|undefined|var|void|while|with|yield)\b/,lookbehind:!0}],function:/#?(?!\s)[_$a-zA-Z\xA0-\uFFFF](?:(?!\s)[$\w\xA0-\uFFFF])*(?=\s*(?:\.\s*(?:apply|bind|call)\s*)?\()/,number:{pattern:RegExp("(^|[^\\w$])(?:NaN|Infinity|0[bB][01]+(?:_[01]+)*n?|0[oO][0-7]+(?:_[0-7]+)*n?|0[xX][\\dA-Fa-f]+(?:_[\\dA-Fa-f]+)*n?|\\d+(?:_\\d+)*n|(?:\\d+(?:_\\d+)*(?:\\.(?:\\d+(?:_\\d+)*)?)?|\\.\\d+(?:_\\d+)*)(?:[Ee][+-]?\\d+(?:_\\d+)*)?)(?![\\w$])"),lookbehind:!0},operator:/--|\+\+|\*\*=?|=>|&&=?|\|\|=?|[!=]==|<<=?|>>>?=?|[-+*/%&|^!=<>]=?|\.{3}|\?\?=?|\?\.?|[~:]/}),Prism.languages.javascript["class-name"][0].pattern=/(\b(?:class|extends|implements|instanceof|interface|new)\s+)[\w.\\]+/,Prism.languages.insertBefore("javascript","keyword",{regex:{pattern:RegExp("((?:^|[^$\\w\\xA0-\\uFFFF.\"'\\])\\s]|\\b(?:return|yield))\\s*)/(?:(?:\\[(?:[^\\]\\\\\r\n]|\\\\.)*\\]|\\\\.|[^/\\\\\\[\r\n])+/[dgimyus]{0,7}|(?:\\[(?:[^[\\]\\\\\r\n]|\\\\.|\\[(?:[^[\\]\\\\\r\n]|\\\\.|\\[(?:[^[\\]\\\\\r\n]|\\\\.)*\\])*\\])*\\]|\\\\.|[^/\\\\\\[\r\n])+/[dgimyus]{0,7}v[dgimyus]{0,7})(?=(?:\\s|/\\*(?:[^*]|\\*(?!/))*\\*/)*(?:$|[\r\n,.;:})\\]]|//))"),lookbehind:!0,greedy:!0,inside:{"regex-source":{pattern:/^(\/)[\s\S]+(?=\/[a-z]*$)/,lookbehind:!0,alias:"language-regex",inside:Prism.languages.regex},"regex-delimiter":/^\/|\/$/,"regex-flags":/^[a-z]+$/}},"function-variable":{pattern:/#?(?!\s)[_$a-zA-Z\xA0-\uFFFF](?:(?!\s)[$\w\xA0-\uFFFF])*(?=\s*[=:]\s*(?:async\s*)?(?:\bfunction\b|(?:\((?:[^()]|\([^()]*\))*\)|(?!\s)[_$a-zA-Z\xA0-\uFFFF](?:(?!\s)[$\w\xA0-\uFFFF])*)\s*=>))/,alias:"function"},parameter:[{pattern:/(function(?:\s+(?!\s)[_$a-zA-Z\xA0-\uFFFF](?:(?!\s)[$\w\xA0-\uFFFF])*)?\s*\(\s*)(?!\s)(?:[^()\s]|\s+(?![\s)])|\([^()]*\))+(?=\s*\))/,lookbehind:!0,inside:Prism.languages.javascript},{pattern:/(^|[^$\w\xA0-\uFFFF])(?!\s)[_$a-z\xA0-\uFFFF](?:(?!\s)[$\w\xA0-\uFFFF])*(?=\s*=>)/i,lookbehind:!0,inside:Prism.languages.javascript},{pattern:/(\(\s*)(?!\s)(?:[^()\s]|\s+(?![\s)])|\([^()]*\))+(?=\s*\)\s*=>)/,lookbehind:!0,inside:Prism.languages.javascript},{pattern:/((?:\b|\s|^)(?!(?:as|async|await|break|case|catch|class|const|continue|debugger|default|delete|do|else|enum|export|extends|finally|for|from|function|get|if|implements|import|in|instanceof|interface|let|new|null|of|package|private|protected|public|return|set|static|super|switch|this|throw|try|typeof|undefined|var|void|while|with|yield)(?![$\w\xA0-\uFFFF]))(?:(?!\s)[_$a-zA-Z\xA0-\uFFFF](?:(?!\s)[$\w\xA0-\uFFFF])*\s*)\(\s*|\]\s*\(\s*)(?!\s)(?:[^()\s]|\s+(?![\s)])|\([^()]*\))+(?=\s*\)\s*\{)/,lookbehind:!0,inside:Prism.languages.javascript}],constant:/\b[A-Z](?:[A-Z_]|\dx?)*\b/}),Prism.languages.insertBefore("javascript","string",{hashbang:{pattern:/^#!.*/,greedy:!0,alias:"comment"},"template-string":{pattern:/`(?:\\[\s\S]|\$\{(?:[^{}]|\{(?:[^{}]|\{[^}]*\})*\})+\}|(?!\$\{)[^\\`])*`/,greedy:!0,inside:{"template-punctuation":{pattern:/^`|`$/,alias:"string"},interpolation:{pattern:/((?:^|[^\\])(?:\\{2})*)\$\{(?:[^{}]|\{(?:[^{}]|\{[^}]*\})*\})+\}/,lookbehind:!0,inside:{"interpolation-punctuation":{pattern:/^\$\{|\}$/,alias:"punctuation"},rest:Prism.languages.javascript}},string:/[\s\S]+/}},"string-property":{pattern:/((?:^|[,{])[ \t]*)(["'])(?:\\(?:\r\n|[\s\S])|(?!\2)[^\\\r\n])*\2(?=\s*:)/m,lookbehind:!0,greedy:!0,alias:"property"}}),Prism.languages.insertBefore("javascript","operator",{"literal-property":{pattern:/((?:^|[,{])[ \t]*)(?!\s)[_$a-zA-Z\xA0-\uFFFF](?:(?!\s)[$\w\xA0-\uFFFF])*(?=\s*:)/m,lookbehind:!0,alias:"property"}}),Prism.languages.markup&&(Prism.languages.markup.tag.addInlined("script","javascript"),Prism.languages.markup.tag.addAttribute("on(?:abort|blur|change|click|composition(?:end|start|update)|dblclick|error|focus(?:in|out)?|key(?:down|up)|load|mouse(?:down|enter|leave|move|out|over|up)|reset|resize|scroll|select|slotchange|submit|unload|wheel)","javascript")),Prism.languages.js=Prism.languages.javascript;
Prism.languages.json={property:{pattern:/(^|[^\\])"(?:\\.|[^\\"\r\n])*"(?=\s*:)/,lookbehind:!0,greedy:!0},string:{pattern:/(^|[^\\])"(?:\\.|[^\\"\r\n])*"(?!\s*:)/,lookbehind:!0,greedy:!0},comment:{pattern:/\/\/.*|\/\*[\s\S]*?(?:\*\/|$)/,greedy:!0},number:/-?\b\d+(?:\.\d+)?(?:e[+-]?\d+)?\b/i,punctuation:/[{}[\],]/,operator:/:/,boolean:/\b(?:false|true)\b/,null:{pattern:/\bnull\b/,alias:"keyword"}},Prism.languages.webmanifest=Prism.languages.json;
!function(n){n.languages.kotlin=n.languages.extend("clike",{keyword:{pattern:/(^|[^.])\b(?:abstract|actual|annotation|as|break|by|catch|class|companion|const|constructor|continue|crossinline|data|do|dynamic|else|enum|expect|external|final|finally|for|fun|get|if|import|in|infix|init|inline|inner|interface|internal|is|lateinit|noinline|null|object|open|operator|out|override|package|private|protected|public|reified|return|sealed|set|super|suspend|tailrec|this|throw|to|try|typealias|val|var|vararg|when|where|while)\b/,lookbehind:!0},function:[{pattern:/(?:`[^\r\n`]+`|\b\w+)(?=\s*\()/,greedy:!0},{pattern:/(\.)(?:`[^\r\n`]+`|\w+)(?=\s*\{)/,lookbehind:!0,greedy:!0}],number:/\b(?:0[xX][\da-fA-F]+(?:_[\da-fA-F]+)*|0[bB][01]+(?:_[01]+)*|\d+(?:_\d+)*(?:\.\d+(?:_\d+)*)?(?:[eE][+-]?\d+(?:_\d+)*)?[fFL]?)\b/,operator:/\+[+=]?|-[-=>]?|==?=?|!(?:!|==?)?|[\/*%<>]=?|[?:]:?|\.\.|&&|\|\||\b(?:and|inv|or|shl|shr|ushr|xor)\b/}),delete n.languages.kotlin["class-name"];var e={"interpolation-punctuation":{pattern:/^\$\{?|\}$/,alias:"punctuation"},expression:{pattern:/[\s\S]+/,inside:n.languages.kotlin}};n.languages.insertBefore("kotlin","string",{"string-literal":[{pattern:/"""(?:[^$]|\$(?:(?!\{)|\{[^{}]*\}))*?"""/,alias:"multiline",inside:{interpolation:{pattern:/\$(?:[a-z_]\w*|\{[^{}]*\})/i,inside:e},string:/[\s\S]+/}},{pattern:/"(?:[^"\\\r\n$]|\\.|\$(?:(?!\{)|\{[^{}]*\}))*"/,alias:"singleline",inside:{interpolation:{pattern:/((?:^|[^\\])(?:\\{2})*)\$(?:[a-z_]\w*|\{[^{}]*\})/i,lookbehind:!0,inside:e},string:/[\s\S]+/}}],char:{pattern:/'(?:[^'\\\r\n]|\\(?:.|u[a-fA-F0-9]{0,4}))'/,greedy:!0}}),delete n.languages.kotlin.string,n.languages.insertBefore("kotlin","keyword",{annotation:{pattern:/\B@(?:\w+:)?(?:[A-Z]\w*|\[[^\]]+\])/,alias:"builtin"}}),n.languages.insertBefore("kotlin","function",{label:{pattern:/\b\w+@|@\w+\b/,alias:"symbol"}}),n.languages.kt=n.languages.kotlin,n.languages.kts=n.languages.kotlin}(Prism);
Prism.languages.properties={comment:/^[ \t]*[#!].*$/m,value:{pattern:/(^[ \t]*(?:\\(?:\r\n|[\s\S])|[^\\\s:=])+(?: *[=:] *(?! )| ))(?:\\(?:\r\n|[\s\S])|[^\\\r\n])+/m,lookbehind:!0,alias:"attr-value"},key:{pattern:/^[ \t]*(?:\\(?:\r\n|[\s\S])|[^\\\s:=])+(?= *[=:]| )/m,alias:"attr-name"},punctuation:/[=:]/};
Prism.languages.python={comment:{pattern:/(^|[^\\])#.*/,lookbehind:!0,greedy:!0},"string-interpolation":{pattern:/(?:f|fr|rf)(?:("""|''')[\s\S]*?\1|("|')(?:\\.|(?!\2)[^\\\r\n])*\2)/i,greedy:!0,inside:{interpolation:{pattern:/((?:^|[^{])(?:\{\{)*)\{(?!\{)(?:[^{}]|\{(?!\{)(?:[^{}]|\{(?!\{)(?:[^{}])+\})+\})+\}/,lookbehind:!0,inside:{"format-spec":{pattern:/(:)[^:(){}]+(?=\}$)/,lookbehind:!0},"conversion-option":{pattern:/![sra](?=[:}]$)/,alias:"punctuation"},rest:null}},string:/[\s\S]+/}},"triple-quoted-string":{pattern:/(?:[rub]|br|rb)?("""|''')[\s\S]*?\1/i,greedy:!0,alias:"string"},string:{pattern:/(?:[rub]|br|rb)?("|')(?:\\.|(?!\1)[^\\\r\n])*\1/i,greedy:!0},function:{pattern:/((?:^|\s)def[ \t]+)[a-zA-Z_]\w*(?=\s*\()/g,lookbehind:!0},"class-name":{pattern:/(\bclass\s+)\w+/i,lookbehind:!0},decorator:{pattern:/(^[\t ]*)@\w+(?:\.\w+)*/m,lookbehind:!0,alias:["annotation","punctuation"],inside:{punctuation:/\./}},keyword:/\b(?:_(?=\s*:)|and|as|assert|async|await|break|case|class|continue|def|del|elif|else|except|exec|finally|for|from|global|if|import|in|is|lambda|match|nonlocal|not|or|pass|print|raise|return|try|while|with|yield)\b/,builtin:/\b(?:__import__|abs|all|any|apply|ascii|basestring|bin|bool|buffer|bytearray|bytes|callable|chr|classmethod|cmp|coerce|compile|complex|delattr|dict|dir|divmod|enumerate|eval|execfile|file|filter|float|format|frozenset|getattr|globals|hasattr|hash|help|hex|id|input|int|intern|isinstance|issubclass|iter|len|list|locals|long|map|max|memoryview|min|next|object|oct|open|ord|pow|property|range|raw_input|reduce|reload|repr|reversed|round|set|setattr|slice|sorted|staticmethod|str|sum|super|tuple|type|unichr|unicode|vars|xrange|zip)\b/,boolean:/\b(?:False|None|True)\b/,number:/\b0(?:b(?:_?[01])+|o(?:_?[0-7])+|x(?:_?[a-f0-9])+)\b|(?:\b\d+(?:_\d+)*(?:\.(?:\d+(?:_\d+)*)?)?|\B\.\d+(?:_\d+)*)(?:e[+-]?\d+(?:_\d+)*)?j?(?!\w)/i,operator:/[-+%=]=?|!=|:=|\*\*?=?|\/\/?=?|<[<=>]?|>[=>]?|[&|^~]/,punctuation:/[{}[\];(),.:]/},Prism.languages.python["string-interpolation"].inside.interpolation.inside.rest=Prism.languages.python,Prism.languages.py=Prism.languages.python;
Prism.languages.sql={comment:{pattern:/(^|[^\\])(?:\/\*[\s\S]*?\*\/|(?:--|\/\/|#).*)/,lookbehind:!0},variable:[{pattern:/@(["'`])(?:\\[\s\S]|(?!\1)[^\\])+\1/,greedy:!0},/@[\w.$]+/],string:{pattern:/(^|[^@\\])("|')(?:\\[\s\S]|(?!\2)[^\\]|\2\2)*\2/,greedy:!0,lookbehind:!0},identifier:{pattern:/(^|[^@\\])`(?:\\[\s\S]|[^`\\]|``)*`/,greedy:!0,lookbehind:!0,inside:{punctuation:/^`|`$/}},function:/\b(?:AVG|COUNT|FIRST|FORMAT|LAST|LCASE|LEN|MAX|MID|MIN|MOD|NOW|ROUND|SUM|UCASE)(?=\s*\()/i,keyword:/\b(?:ACTION|ADD|AFTER|ALGORITHM|ALL|ALTER|ANALYZE|ANY|APPLY|AS|ASC|AUTHORIZATION|AUTO_INCREMENT|BACKUP|BDB|BEGIN|BERKELEYDB|BIGINT|BINARY|BIT|BLOB|BOOL|BOOLEAN|BREAK|BROWSE|BTREE|BULK|BY|CALL|CASCADED?|CASE|CHAIN|CHAR(?:ACTER|SET)?|CHECK(?:POINT)?|CLOSE|CLUSTERED|COALESCE|COLLATE|COLUMNS?|COMMENT|COMMIT(?:TED)?|COMPUTE|CONNECT|CONSISTENT|CONSTRAINT|CONTAINS(?:TABLE)?|CONTINUE|CONVERT|CREATE|CROSS|CURRENT(?:_DATE|_TIME|_TIMESTAMP|_USER)?|CURSOR|CYCLE|DATA(?:BASES?)?|DATE(?:TIME)?|DAY|DBCC|DEALLOCATE|DEC|DECIMAL|DECLARE|DEFAULT|DEFINER|DELAYED|DELETE|DELIMITERS?|DENY|DESC|DESCRIBE|DETERMINISTIC|DISABLE|DISCARD|DISK|DISTINCT|DISTINCTROW|DISTRIBUTED|DO|DOUBLE|DROP|DUMMY|DUMP(?:FILE)?|DUPLICATE|ELSE(?:IF)?|ENABLE|ENCLOSED|END|ENGINE|ENUM|ERRLVL|ERRORS|ESCAPED?|EXCEPT|EXEC(?:UTE)?|EXISTS|EXIT|EXPLAIN|EXTENDED|FETCH|FIELDS|FILE|FILLFACTOR|FIRST|FIXED|FLOAT|FOLLOWING|FOR(?: EACH ROW)?|FORCE|FOREIGN|FREETEXT(?:TABLE)?|FROM|FULL|FUNCTION|GEOMETRY(?:COLLECTION)?|GLOBAL|GOTO|GRANT|GROUP|HANDLER|HASH|HAVING|HOLDLOCK|HOUR|IDENTITY(?:COL|_INSERT)?|IF|IGNORE|IMPORT|INDEX|INFILE|INNER|INNODB|INOUT|INSERT|INT|INTEGER|INTERSECT|INTERVAL|INTO|INVOKER|ISOLATION|ITERATE|JOIN|KEYS?|KILL|LANGUAGE|LAST|LEAVE|LEFT|LEVEL|LIMIT|LINENO|LINES|LINESTRING|LOAD|LOCAL|LOCK|LONG(?:BLOB|TEXT)|LOOP|MATCH(?:ED)?|MEDIUM(?:BLOB|INT|TEXT)|MERGE|MIDDLEINT|MINUTE|MODE|MODIFIES|MODIFY|MONTH|MULTI(?:LINESTRING|POINT|POLYGON)|NATIONAL|NATURAL|NCHAR|NEXT|NO|NONCLUSTERED|NULLIF|NUMERIC|OFF?|OFFSETS?|ON|OPEN(?:DATASOURCE|QUERY|ROWSET)?|OPTIMIZE|OPTION(?:ALLY)?|ORDER|OUT(?:ER|FILE)?|OVER|PARTIAL|PARTITION|PERCENT|PIVOT|PLAN|POINT|POLYGON|PRECEDING|PRECISION|PREPARE|PREV|PRIMARY|PRINT|PRIVILEGES|PROC(?:EDURE)?|PUBLIC|PURGE|QUICK|RAISERROR|READS?|REAL|RECONFIGURE|REFERENCES|RELEASE|RENAME|REPEAT(?:ABLE)?|REPLACE|REPLICATION|REQUIRE|RESIGNAL|RESTORE|RESTRICT|RETURN(?:ING|S)?|REVOKE|RIGHT|ROLLBACK|ROUTINE|ROW(?:COUNT|GUIDCOL|S)?|RTREE|RULE|SAVE(?:POINT)?|SCHEMA|SECOND|SELECT|SERIAL(?:IZABLE)?|SESSION(?:_USER)?|SET(?:USER)?|SHARE|SHOW|SHUTDOWN|SIMPLE|SMALLINT|SNAPSHOT|SOME|SONAME|SQL|START(?:ING)?|STATISTICS|STATUS|STRIPED|SYSTEM_USER|TABLES?|TABLESPACE|TEMP(?:ORARY|TABLE)?|TERMINATED|TEXT(?:SIZE)?|THEN|TIME(?:STAMP)?|TINY(?:BLOB|INT|TEXT)|TOP?|TRAN(?:SACTIONS?)?|TRIGGER|TRUNCATE|TSEQUAL|TYPES?|UNBOUNDED|UNCOMMITTED|UNDEFINED|UNION|UNIQUE|UNLOCK|UNPIVOT|UNSIGNED|UPDATE(?:TEXT)?|USAGE|USE|USER|USING|VALUES?|VAR(?:BINARY|CHAR|CHARACTER|YING)|VIEW|WAITFOR|WARNINGS|WHEN|WHERE|WHILE|WITH(?: ROLLUP|IN)?|WORK|WRITE(?:TEXT)?|YEAR)\b/i,boolean:/\b(?:FALSE|NULL|TRUE)\b/i,number:/\b0x[\da-f]+\b|\b\d+(?:\.\d*)?|\B\.\d+\b/i,operator:/[-+*\/=%^~]|&&?|\|\|?|!=?|<(?:=>?|<|>)?|>[>=]?|\b(?:AND|BETWEEN|DIV|ILIKE|IN|IS|LIKE|NOT|OR|REGEXP|RLIKE|SOUNDS LIKE|XOR)\b/i,punctuation:/[;[\]()`,.]/};

/* Plyr 3.8.4 -- plyr.io -- MIT, see plyr.LICENSE beside this file.
   VENDORED, UNMODIFIED apart from this header. Do not edit: re-vendor with
   `npm pack plyr` and take dist/plyr.min.js, dist/plyr.css and dist/plyr.svg.
   ⛔ It is configured with loadSprite:false and iconUrl:"" in video-player.js
   so it never fetches cdn.plyr.io -- the study site must work on file://. */
"object"==typeof navigator&&function(e,t){"object"==typeof exports&&"undefined"!=typeof module?module.exports=t():"function"==typeof define&&define.amd?define("Plyr",t):(e="undefined"!=typeof globalThis?globalThis:e||self).Plyr=t()}(this,function(){"use strict";function e(e,t,i){return(t=function(e){var t=function(e,t){if("object"!=typeof e||!e)return e;var i=e[Symbol.toPrimitive];if(void 0!==i){var s=i.call(e,t);if("object"!=typeof s)return s;throw new TypeError("@@toPrimitive must return a primitive value.")}return("string"===t?String:Number)(e)}(e,"string");return"symbol"==typeof t?t:t+""}(t))in e?Object.defineProperty(e,t,{value:i,enumerable:!0,configurable:!0,writable:!0}):e[t]=i,e}function t(e,t){for(var i=0;i<t.length;i++){var s=t[i];s.enumerable=s.enumerable||!1,s.configurable=!0,"value"in s&&(s.writable=!0),Object.defineProperty(e,s.key,s)}}function i(e,t,i){return t in e?Object.defineProperty(e,t,{value:i,enumerable:!0,configurable:!0,writable:!0}):e[t]=i,e}function s(e,t){var i=Object.keys(e);if(Object.getOwnPropertySymbols){var s=Object.getOwnPropertySymbols(e);t&&(s=s.filter(function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable})),i.push.apply(i,s)}return i}function n(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?s(Object(n),!0).forEach(function(t){i(e,t,n[t])}):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):s(Object(n)).forEach(function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))})}return e}var a={addCSS:!0,thumbWidth:15,watch:!0};var l=function(e){return null!=e?e.constructor:null},r=function(e,t){return!!(e&&t&&e instanceof t)},o=function(e){return null==e},c=function(e){return l(e)===Object},u=function(e){return l(e)===String},h=function(e){return Array.isArray(e)},d=function(e){return r(e,NodeList)},m=u,p=h,g=d,f=function(e){return r(e,Element)},y=function(e){return r(e,Event)},b=function(e){return o(e)||(u(e)||h(e)||d(e))&&!e.length||c(e)&&!Object.keys(e).length};function v(e,t){if(1>t){var i=function(e){var t="".concat(e).match(/(?:\.(\d+))?(?:[eE]([+-]?\d+))?$/);return t?Math.max(0,(t[1]?t[1].length:0)-(t[2]?+t[2]:0)):0}(t);return parseFloat(e.toFixed(i))}return Math.round(e/t)*t}var w=function(){function e(t,i){(function(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")})(this,e),f(t)?this.element=t:m(t)&&(this.element=document.querySelector(t)),f(this.element)&&b(this.element.rangeTouch)&&(this.config=n({},a,{},i),this.init())}return function(e,i,s){i&&t(e.prototype,i),s&&t(e,s)}(e,[{key:"init",value:function(){e.enabled&&(this.config.addCSS&&(this.element.style.userSelect="none",this.element.style.webKitUserSelect="none",this.element.style.touchAction="manipulation"),this.listeners(!0),this.element.rangeTouch=this)}},{key:"destroy",value:function(){e.enabled&&(this.config.addCSS&&(this.element.style.userSelect="",this.element.style.webKitUserSelect="",this.element.style.touchAction=""),this.listeners(!1),this.element.rangeTouch=null)}},{key:"listeners",value:function(e){var t=this,i=e?"addEventListener":"removeEventListener";["touchstart","touchmove","touchend"].forEach(function(e){t.element[i](e,function(e){return t.set(e)},!1)})}},{key:"get",value:function(t){if(!e.enabled||!y(t))return null;var i,s=t.target,n=t.changedTouches[0],a=parseFloat(s.getAttribute("min"))||0,l=parseFloat(s.getAttribute("max"))||100,r=parseFloat(s.getAttribute("step"))||1,o=s.getBoundingClientRect(),c=100/o.width*(this.config.thumbWidth/2)/100;return 0>(i=100/o.width*(n.clientX-o.left))?i=0:100<i&&(i=100),50>i?i-=(100-2*i)*c:50<i&&(i+=2*(i-50)*c),a+v(i/100*(l-a),r)}},{key:"set",value:function(t){e.enabled&&y(t)&&!t.target.disabled&&(t.preventDefault(),t.target.value=this.get(t),function(e,t){if(e&&t){var i=new Event(t,{bubbles:!0});e.dispatchEvent(i)}}(t.target,"touchend"===t.type?"change":"input"))}}],[{key:"setup",value:function(t){var i=1<arguments.length&&void 0!==arguments[1]?arguments[1]:{},s=null;if(b(t)||m(t)?s=Array.from(document.querySelectorAll(m(t)?t:'input[type="range"]')):f(t)?s=[t]:g(t)?s=Array.from(t):p(t)&&(s=t.filter(f)),b(s))return null;var l=n({},a,{},i);if(m(t)&&l.watch){var r=new MutationObserver(function(i){Array.from(i).forEach(function(i){Array.from(i.addedNodes).forEach(function(i){f(i)&&function(e,t){return function(){return Array.from(document.querySelectorAll(t)).includes(this)}.call(e,t)}(i,t)&&new e(i,l)})})});r.observe(document.body,{childList:!0,subtree:!0})}return s.map(function(t){return new e(t,i)})}},{key:"enabled",get:function(){return"ontouchstart"in document.documentElement}}]),e}();const k=e=>null!=e?e.constructor:null,T=(e,t)=>Boolean(e&&t&&e instanceof t),C=e=>null==e,A=e=>k(e)===Object,S=e=>k(e)===String,E=e=>"function"==typeof e,P=e=>Array.isArray(e),M=e=>T(e,NodeList);function N(e){return C(e)||(S(e)||P(e)||M(e))&&!e.length||A(e)&&!Object.keys(e).length}var x={nullOrUndefined:C,object:A,number:e=>k(e)===Number&&!Number.isNaN(e),string:S,boolean:e=>k(e)===Boolean,function:E,array:P,weakMap:e=>T(e,WeakMap),nodeList:M,element:function(e){return null!==e&&"object"==typeof e&&1===e.nodeType&&"object"==typeof e.style&&"object"==typeof e.ownerDocument},textNode:e=>k(e)===Text,event:e=>T(e,Event),keyboardEvent:e=>T(e,KeyboardEvent),cue:e=>T(e,window.TextTrackCue)||T(e,window.VTTCue),track:e=>T(e,TextTrack)||!C(e)&&S(e.kind),promise:e=>T(e,Promise)&&E(e.then),url:function(e){if(T(e,window.URL))return!0;if(!S(e))return!1;let t=e;e.startsWith("http://")&&e.startsWith("https://")||(t=`http://${e}`);try{return!N(new URL(t).hostname)}catch{return!1}},empty:N};const L=(()=>{const e=document.createElement("span"),t={WebkitTransition:"webkitTransitionEnd",MozTransition:"transitionend",OTransition:"oTransitionEnd otransitionend",transition:"transitionend"},i=Object.keys(t).find(t=>void 0!==e.style[t]);return!!x.string(i)&&t[i]})();function I(e,t){setTimeout(()=>{try{e.hidden=!0,e.offsetHeight,e.hidden=!1}catch{}},t)}function $(e,t){return t.split(".").reduce((e,t)=>e&&e[t],e)}function _(e={},...t){if(!t.length)return e;const i=t.shift();return x.object(i)?(Object.keys(i).forEach(t=>{x.object(i[t])?(Object.keys(e).includes(t)||Object.assign(e,{[t]:{}}),_(e[t],i[t])):Object.assign(e,{[t]:i[t]})}),_(e,...t)):e}function O(e,t){const i=e.length?e:[e];Array.from(i).reverse().forEach((e,i)=>{const s=i>0?t.cloneNode(!0):t,n=e.parentNode,a=e.nextSibling;s.appendChild(e),a?n.insertBefore(s,a):n.appendChild(s)})}function j(e,t){x.element(e)&&!x.empty(t)&&Object.entries(t).filter(([,e])=>!x.nullOrUndefined(e)).forEach(([t,i])=>e.setAttribute(t,i))}function q(e,t,i){const s=document.createElement(e);return x.object(t)&&j(s,t),x.string(i)&&(s.textContent=i),s}function D(e,t,i,s){x.element(t)&&t.appendChild(q(e,i,s))}function H(e){x.nodeList(e)||x.array(e)?Array.from(e).forEach(H):x.element(e)&&x.element(e.parentNode)&&e.parentNode.removeChild(e)}function R(e){if(!x.element(e))return;let{length:t}=e.childNodes;for(;t>0;)e.removeChild(e.lastChild),t-=1}function F(e,t){return x.element(t)&&x.element(t.parentNode)&&x.element(e)?(t.parentNode.replaceChild(e,t),e):null}function V(e,t){if(!x.string(e)||x.empty(e))return{};const i={},s=_({},t);return e.split(",").forEach(e=>{const t=e.trim(),n=t.replace(".",""),a=t.replace(/[[\]]/g,"").split("="),[l]=a,r=a.length>1?a[1].replace(/["']/g,""):"";switch(t.charAt(0)){case".":x.string(s.class)?i.class=`${s.class} ${n}`:i.class=n;break;case"#":i.id=t.replace("#","");break;case"[":i[l]=r}}),_(s,i)}function U(e,t){if(!x.element(e))return;let i=t;x.boolean(i)||(i=!e.hidden),e.hidden=i}function B(e,t,i){if(x.nodeList(e))return Array.from(e).map(e=>B(e,t,i));if(x.element(e)){let s="toggle";return void 0!==i&&(s=i?"add":"remove"),e.classList[s](t),e.classList.contains(t)}return!1}function W(e,t){return x.element(e)&&e.classList.contains(t)}function z(e,t){const{prototype:i}=Element;return(i.matches||i.webkitMatchesSelector||i.mozMatchesSelector||i.msMatchesSelector||function(){return Array.from(document.querySelectorAll(t)).includes(this)}).call(e,t)}function K(e){return this.elements.container.querySelectorAll(e)}function Y(e){return this.elements.container.querySelector(e)}function X(e=null,t=!1){x.element(e)&&e.focus({preventScroll:!0,focusVisible:t})}const Q={"audio/ogg":"vorbis","audio/wav":"1","video/webm":"vp8, vorbis","video/mp4":"avc1.42E01E, mp4a.40.2","video/ogg":"theora"},J={audio:"canPlayType"in document.createElement("audio"),video:"canPlayType"in document.createElement("video"),check(e,t){const i=J[e]||"html5"!==t;return{api:i,ui:i&&J.rangeInput}},pip:document.pictureInPictureEnabled&&!q("video").disablePictureInPicture,airplay:x.function(window.WebKitPlaybackTargetAvailabilityEvent),playsinline:"playsInline"in document.createElement("video"),mime(e){if(x.empty(e))return!1;const[t]=e.split("/");let i=e;if(!this.isHTML5||t!==this.type)return!1;Object.keys(Q).includes(i)&&(i+=`; codecs="${Q[e]}"`);try{return Boolean(i&&this.media.canPlayType(i).replace(/no/,""))}catch{return!1}},textTracks:"textTracks"in document.createElement("video"),rangeInput:(()=>{const e=document.createElement("input");return e.type="range","range"===e.type})(),touch:"ontouchstart"in document.documentElement,transitions:!1!==L,reducedMotion:"matchMedia"in window&&window.matchMedia("(prefers-reduced-motion)").matches},G=(()=>{let e=!1;try{const t=Object.defineProperty({},"passive",{get:()=>(e=!0,null)});window.addEventListener("test",null,t),window.removeEventListener("test",null,t)}catch{}return e})();function Z(e,t,i,s=!1,n=!0,a=!1){if(!e||!("addEventListener"in e)||x.empty(t)||!x.function(i))return;const l=t.split(" ");let r=a;G&&(r={passive:n,capture:a}),l.forEach(t=>{this&&this.eventListeners&&s&&this.eventListeners.push({element:e,type:t,callback:i,options:r}),e[s?"addEventListener":"removeEventListener"](t,i,r)})}function ee(e,t="",i,s=!0,n=!1){Z.call(this,e,t,i,!0,s,n)}function te(e,t="",i,s=!0,n=!1){Z.call(this,e,t,i,!1,s,n)}function ie(e,t="",i,s=!0,n=!1){const a=(...l)=>{te(e,t,a,s,n),i.apply(this,l)};Z.call(this,e,t,a,!0,s,n)}function se(e,t="",i=!1,s={}){if(!x.element(e)||x.empty(t))return;const n=new CustomEvent(t,{bubbles:i,detail:{...s,plyr:this}});e.dispatchEvent(n)}function ne(){this&&this.eventListeners&&(this.eventListeners.forEach(e=>{const{element:t,type:i,callback:s,options:n}=e;t.removeEventListener(i,s,n)}),this.eventListeners=[])}function ae(){return new Promise(e=>this.ready?setTimeout(e,0):ee.call(this,this.elements.container,"ready",e)).then(()=>{})}function le(e){x.promise(e)&&e.then(null,()=>{})}function re(e){return x.array(e)?e.filter((t,i)=>e.indexOf(t)===i):e}function oe(e,t){return x.array(e)&&e.length?e.reduce((e,i)=>Math.abs(i-t)<Math.abs(e-t)?i:e):null}function ce(e){return!(!window||!window.CSS)&&window.CSS.supports(e)}const ue=[[1,1],[4,3],[3,4],[5,4],[4,5],[3,2],[2,3],[16,10],[10,16],[16,9],[9,16],[21,9],[9,21],[32,9],[9,32]].reduce((e,[t,i])=>({...e,[t/i]:[t,i]}),{});function he(e){if(!(x.array(e)||x.string(e)&&e.includes(":")))return!1;return(x.array(e)?e:e.split(":")).map(Number).every(x.number)}function de(e){if(!x.array(e)||!e.every(x.number))return null;const[t,i]=e,s=(e,t)=>0===t?e:s(t,e%t),n=s(t,i);return[t/n,i/n]}function me(e){const t=e=>he(e)?e.split(":").map(Number):null;let i=t(e);if(null===i&&(i=t(this.config.ratio)),null===i&&!x.empty(this.embed)&&x.array(this.embed.ratio)&&({ratio:i}=this.embed),null===i&&this.isHTML5){const{videoWidth:e,videoHeight:t}=this.media;i=[e,t]}return de(i)}function pe(e){if(!this.isVideo)return{};const{wrapper:t}=this.elements,i=me.call(this,e);if(!x.array(i))return{};const[s,n]=de(i),a=100/s*n;if(ce(`aspect-ratio: ${s}/${n}`)?t.style.aspectRatio=`${s}/${n}`:t.style.paddingBottom=`${a}%`,this.isVimeo&&!this.config.vimeo.premium&&this.supported.ui){const e=100/this.media.offsetWidth*Number.parseInt(window.getComputedStyle(this.media).paddingBottom,10),i=(e-a)/(e/50);this.fullscreen.active?t.style.paddingBottom=null:this.media.style.transform=`translateY(-${i}%)`}else this.isHTML5&&t.classList.add(this.config.classNames.videoFixedRatio);return{padding:a,ratio:i}}function ge(e,t,i=.05){const s=e/t,n=oe(Object.keys(ue),s);return Math.abs(n-s)<=i?ue[n]:[e,t]}const fe={getSources(){if(!this.isHTML5)return[];return Array.from(this.media.querySelectorAll("source")).filter(e=>{const t=e.getAttribute("type");return!!x.empty(t)||J.mime.call(this,t)})},getQualityOptions(){return this.config.quality.forced?this.config.quality.options:fe.getSources.call(this).map(e=>Number(e.getAttribute("size"))).filter(Boolean)},setup(){if(!this.isHTML5)return;const e=this;e.options.speed=e.config.speed.options,x.empty(this.config.ratio)||pe.call(e),Object.defineProperty(e.media,"quality",{get(){const t=fe.getSources.call(e).find(t=>t.getAttribute("src")===e.source);return t&&Number(t.getAttribute("size"))},set(t){if(e.quality!==t){if(e.config.quality.forced&&x.function(e.config.quality.onChange))e.config.quality.onChange(t);else{const i=fe.getSources.call(e).find(e=>Number(e.getAttribute("size"))===t);if(!i)return;const{currentTime:s,paused:n,preload:a,readyState:l,playbackRate:r}=e.media;e.media.src=i.getAttribute("src"),("none"!==a||l)&&(e.once("loadedmetadata",()=>{e.speed=r,e.currentTime=s,n||le(e.play())}),e.media.load())}se.call(e,e.media,"qualitychange",!1,{quality:t})}}})},cancelRequests(){this.isHTML5&&(H(fe.getSources.call(this)),this.media.setAttribute("src",this.config.blankVideo),this.media.load(),this.debug.log("Cancelled network requests"))}};var ye={isIE:Boolean(window.document.documentMode),isEdge:/Edge/.test(navigator.userAgent),isWebKit:"WebkitAppearance"in document.documentElement.style&&!/Edge/.test(navigator.userAgent),isIPadOS:"MacIntel"===navigator.platform&&navigator.maxTouchPoints>1,isIos:/iPad|iPhone|iPod/i.test(navigator.userAgent)&&navigator.maxTouchPoints>1};function be(e,...t){return x.empty(e)?e:e.toString().replace(/\{(\d+)\}/g,(e,i)=>t[i].toString())}function ve(e="",t="",i=""){return e.replace(new RegExp(t.toString().replace(/([.*+?^=!:${}()|[\]/\\])/g,"\\$1"),"g"),i.toString())}function we(e=""){return e.toString().replace(/\w\S*/g,e=>e.charAt(0).toUpperCase()+e.slice(1).toLowerCase())}function ke(e=""){let t=e.toString();return t=function(e=""){let t=e.toString();return t=ve(t,"-"," "),t=ve(t,"_"," "),t=we(t),ve(t," ","")}(t),t.charAt(0).toLowerCase()+t.slice(1)}function Te(e){const t=document.createElement("div");return t.appendChild(e),t.innerHTML}const Ce={pip:"PIP",airplay:"AirPlay",html5:"HTML5",vimeo:"Vimeo",youtube:"YouTube"},Ae={get(e="",t={}){if(x.empty(e)||x.empty(t))return"";let i=$(t.i18n,e);if(x.empty(i))return Object.keys(Ce).includes(e)?Ce[e]:"";const s={"{seektime}":t.seekTime,"{title}":t.title};return Object.entries(s).forEach(([e,t])=>{i=ve(i,e,t)}),i}};class Se{constructor(t){e(this,"get",e=>{if(!Se.supported||!this.enabled)return null;const t=window.localStorage.getItem(this.key);if(x.empty(t))return null;const i=JSON.parse(t);return x.string(e)&&e.length?i[e]:i}),e(this,"set",e=>{if(!Se.supported||!this.enabled)return;if(!x.object(e))return;let t=this.get();x.empty(t)&&(t={}),_(t,e);try{window.localStorage.setItem(this.key,JSON.stringify(t))}catch{}}),this.enabled=t.config.storage.enabled,this.key=t.config.storage.key}static get supported(){try{if(!("localStorage"in window))return!1;const e="___test";return window.localStorage.setItem(e,e),window.localStorage.removeItem(e),!0}catch{return!1}}}function Ee(e,t="text",i=!1){return new Promise((s,n)=>{try{const n=new XMLHttpRequest;if(!("withCredentials"in n))return;i&&(n.withCredentials=!0),n.addEventListener("load",()=>{if("text"===t)try{s(JSON.parse(n.responseText))}catch{s(n.responseText)}else s(n.response)}),n.addEventListener("error",()=>{throw new Error(n.status)}),n.open("GET",e,!0),n.responseType=t,n.send()}catch(e){n(e)}})}function Pe(e,t){if(!x.string(e))return;const i="cache",s=x.string(t);let n=!1;const a=()=>null!==document.getElementById(t),l=(e,t)=>{e.innerHTML=t,s&&a()||document.body.insertAdjacentElement("afterbegin",e)};if(!s||!a()){const a=Se.supported,r=document.createElement("div");if(r.setAttribute("hidden",""),s&&r.setAttribute("id",t),a){const e=window.localStorage.getItem(`${i}-${t}`);if(n=null!==e,n){const t=JSON.parse(e);l(r,t.content)}}Ee(e).then(e=>{if(!x.empty(e)){if(a)try{window.localStorage.setItem(`${i}-${t}`,JSON.stringify({content:e}))}catch{}l(r,e)}}).catch(()=>{})}}const Me=e=>Math.trunc(e/60/60%60,10);function Ne(e=0,t=!1,i=!1){if(!x.number(e))return Ne(void 0,t,i);const s=e=>`0${e}`.slice(-2);let n=Me(e);const a=(l=e,Math.trunc(l/60%60,10));var l;const r=(e=>Math.trunc(e%60,10))(e);return n=t||n>0?`${n}:`:"",`${i&&e>0?"-":""}${n}${s(a)}:${s(r)}`}const xe={getIconUrl(){const e=new URL(this.config.iconUrl,window.location),t=window.location.host?window.location.host:window.top.location.host,i=e.host!==t||ye.isIE&&!window.svg4everybody;return{url:this.config.iconUrl,cors:i}},findElements(){try{return this.elements.controls=Y.call(this,this.config.selectors.controls.wrapper),this.elements.buttons={play:K.call(this,this.config.selectors.buttons.play),pause:Y.call(this,this.config.selectors.buttons.pause),restart:Y.call(this,this.config.selectors.buttons.restart),rewind:Y.call(this,this.config.selectors.buttons.rewind),fastForward:Y.call(this,this.config.selectors.buttons.fastForward),mute:Y.call(this,this.config.selectors.buttons.mute),pip:Y.call(this,this.config.selectors.buttons.pip),airplay:Y.call(this,this.config.selectors.buttons.airplay),settings:Y.call(this,this.config.selectors.buttons.settings),captions:Y.call(this,this.config.selectors.buttons.captions),fullscreen:Y.call(this,this.config.selectors.buttons.fullscreen)},this.elements.progress=Y.call(this,this.config.selectors.progress),this.elements.inputs={seek:Y.call(this,this.config.selectors.inputs.seek),volume:Y.call(this,this.config.selectors.inputs.volume)},this.elements.display={buffer:Y.call(this,this.config.selectors.display.buffer),currentTime:Y.call(this,this.config.selectors.display.currentTime),duration:Y.call(this,this.config.selectors.display.duration)},x.element(this.elements.progress)&&(this.elements.display.seekTooltip=this.elements.progress.querySelector(`.${this.config.classNames.tooltip}`)),!0}catch(e){return this.debug.warn("It looks like there is a problem with your custom controls HTML",e),this.toggleNativeControls(!0),!1}},createIcon(e,t){const i="http://www.w3.org/2000/svg",s=xe.getIconUrl.call(this),n=`${s.cors?"":s.url}#${this.config.iconPrefix}`,a=document.createElementNS(i,"svg");j(a,_(t,{"aria-hidden":"true",focusable:"false"}));const l=document.createElementNS(i,"use"),r=`${n}-${e}`;return"href"in l&&l.setAttributeNS("http://www.w3.org/1999/xlink","href",r),l.setAttributeNS("http://www.w3.org/1999/xlink","xlink:href",r),a.appendChild(l),a},createLabel(e,t={}){const i=Ae.get(e,this.config);return q("span",{...t,class:[t.class,this.config.classNames.hidden].filter(Boolean).join(" ")},i)},createBadge(e){if(x.empty(e))return null;const t=q("span",{class:this.config.classNames.menu.value});return t.appendChild(q("span",{class:this.config.classNames.menu.badge},e)),t},createButton(e,t){const i=_({},t);let s=ke(e);const n={element:"button",toggle:!1,label:null,icon:null,labelPressed:null,iconPressed:null};switch(["element","icon","label"].forEach(e=>{Object.keys(i).includes(e)&&(n[e]=i[e],delete i[e])}),"button"!==n.element||Object.keys(i).includes("type")||(i.type="button"),Object.keys(i).includes("class")?i.class.split(" ").includes(this.config.classNames.control)||_(i,{class:`${i.class} ${this.config.classNames.control}`}):i.class=this.config.classNames.control,e){case"play":n.toggle=!0,n.label="play",n.labelPressed="pause",n.icon="play",n.iconPressed="pause";break;case"mute":n.toggle=!0,n.label="mute",n.labelPressed="unmute",n.icon="volume",n.iconPressed="muted";break;case"captions":n.toggle=!0,n.label="enableCaptions",n.labelPressed="disableCaptions",n.icon="captions-off",n.iconPressed="captions-on";break;case"fullscreen":n.toggle=!0,n.label="enterFullscreen",n.labelPressed="exitFullscreen",n.icon="enter-fullscreen",n.iconPressed="exit-fullscreen";break;case"play-large":i.class+=` ${this.config.classNames.control}--overlaid`,s="play",n.label="play",n.icon="play";break;default:x.empty(n.label)&&(n.label=s),x.empty(n.icon)&&(n.icon=e)}const a=q(n.element);return n.toggle?(a.appendChild(xe.createIcon.call(this,n.iconPressed,{class:"icon--pressed"})),a.appendChild(xe.createIcon.call(this,n.icon,{class:"icon--not-pressed"})),a.appendChild(xe.createLabel.call(this,n.labelPressed,{class:"label--pressed"})),a.appendChild(xe.createLabel.call(this,n.label,{class:"label--not-pressed"}))):(a.appendChild(xe.createIcon.call(this,n.icon)),a.appendChild(xe.createLabel.call(this,n.label))),_(i,V(this.config.selectors.buttons[s],i)),j(a,i),"play"===s?(x.array(this.elements.buttons[s])||(this.elements.buttons[s]=[]),this.elements.buttons[s].push(a)):this.elements.buttons[s]=a,a},createRange(e,t){const i=q("input",_(V(this.config.selectors.inputs[e]),{type:"range",min:0,max:100,step:.01,value:0,autocomplete:"off",role:"slider","aria-label":Ae.get(e,this.config),"aria-valuemin":0,"aria-valuemax":100,"aria-valuenow":0},t));return this.elements.inputs[e]=i,xe.updateRangeFill.call(this,i),w.setup(i),i},createProgress(e,t){const i=q("progress",_(V(this.config.selectors.display[e]),{min:0,max:100,value:0,role:"progressbar","aria-hidden":!0},t));if("volume"!==e){i.appendChild(q("span",null,"0"));const t={played:"played",buffer:"buffered"}[e],s=t?Ae.get(t,this.config):"";i.textContent=`% ${s.toLowerCase()}`}return this.elements.display[e]=i,i},createTime(e,t){const i=V(this.config.selectors.display[e],t),s=q("div",_(i,{class:`${i.class?i.class:""} ${this.config.classNames.display.time} `.trim(),"aria-label":Ae.get(e,this.config),role:"timer"}),"00:00");return this.elements.display[e]=s,s},bindMenuItemShortcuts(e,t){ee.call(this,e,"keydown keyup",i=>{if(![" ","ArrowUp","ArrowDown","ArrowRight"].includes(i.key))return;if(i.preventDefault(),i.stopPropagation(),"keydown"===i.type)return;const s=z(e,'[role="menuitemradio"]');if(!s&&[" ","ArrowRight"].includes(i.key))xe.showMenuPanel.call(this,t,!0);else{let t;" "!==i.key&&("ArrowDown"===i.key||s&&"ArrowRight"===i.key?(t=e.nextElementSibling,x.element(t)||(t=e.parentNode.firstElementChild)):(t=e.previousElementSibling,x.element(t)||(t=e.parentNode.lastElementChild)),X.call(this,t,!0))}},!1),ee.call(this,e,"keyup",e=>{"Return"===e.key&&xe.focusFirstMenuItem.call(this,null,!0)})},createMenuItem({value:e,list:t,type:i,title:s,badge:n=null,checked:a=!1}){const l=V(this.config.selectors.inputs[i]),r=q("button",_(l,{type:"button",role:"menuitemradio",class:`${this.config.classNames.control} ${l.class?l.class:""}`.trim(),"aria-checked":a,value:e})),o=q("span");o.innerHTML=s,x.element(n)&&o.appendChild(n),r.appendChild(o),Object.defineProperty(r,"checked",{enumerable:!0,get:()=>"true"===r.getAttribute("aria-checked"),set(e){e&&Array.from(r.parentNode.children).filter(e=>z(e,'[role="menuitemradio"]')).forEach(e=>e.setAttribute("aria-checked","false")),r.setAttribute("aria-checked",e?"true":"false")}}),this.listeners.bind(r,"click keyup",t=>{if(!x.keyboardEvent(t)||" "===t.key){switch(t.preventDefault(),t.stopPropagation(),r.checked=!0,i){case"language":this.currentTrack=Number(e);break;case"quality":this.quality=e;break;case"speed":this.speed=Number.parseFloat(e)}xe.showMenuPanel.call(this,"home",x.keyboardEvent(t))}},i,!1),xe.bindMenuItemShortcuts.call(this,r,i),t.appendChild(r)},formatTime(e=0,t=!1){if(!x.number(e))return e;return Ne(e,Me(this.duration)>0,t)},updateTimeDisplay(e=null,t=0,i=!1){x.element(e)&&x.number(t)&&(e.textContent=xe.formatTime(t,i))},updateVolume(){this.supported.ui&&(x.element(this.elements.inputs.volume)&&xe.setRange.call(this,this.elements.inputs.volume,this.muted?0:this.volume),x.element(this.elements.buttons.mute)&&(this.elements.buttons.mute.pressed=this.muted||0===this.volume))},setRange(e,t=0){x.element(e)&&(e.value=t,xe.updateRangeFill.call(this,e))},updateProgress(e){if(!this.supported.ui||!x.event(e))return;let t=0;const i=(e,t)=>{const i=x.number(t)?t:0,s=x.element(e)?e:this.elements.display.buffer;if(x.element(s)){s.value=i;const e=s.getElementsByTagName("span")[0];x.element(e)&&(e.childNodes[0].nodeValue=i)}};if(e)switch(e.type){case"timeupdate":case"seeking":case"seeked":s=this.currentTime,n=this.duration,t=0===s||0===n||Number.isNaN(s)||Number.isNaN(n)?0:(s/n*100).toFixed(2),"timeupdate"===e.type&&xe.setRange.call(this,this.elements.inputs.seek,t);break;case"playing":case"progress":i(this.elements.display.buffer,100*this.buffered)}var s,n},updateRangeFill(e){const t=x.event(e)?e.target:e;if(x.element(t)&&"range"===t.getAttribute("type")){if(z(t,this.config.selectors.inputs.seek)){t.setAttribute("aria-valuenow",this.currentTime);const e=xe.formatTime(this.currentTime),i=xe.formatTime(this.duration),s=Ae.get("seekLabel",this.config);t.setAttribute("aria-valuetext",s.replace("{currentTime}",e).replace("{duration}",i))}else if(z(t,this.config.selectors.inputs.volume)){const e=100*t.value;t.setAttribute("aria-valuenow",e),t.setAttribute("aria-valuetext",`${e.toFixed(1)}%`)}else t.setAttribute("aria-valuenow",t.value);(ye.isWebKit||ye.isIPadOS)&&t.style.setProperty("--value",t.value/t.max*100+"%")}},updateSeekTooltip(e){var t,i;if(!this.config.tooltips.seek||!x.element(this.elements.inputs.seek)||!x.element(this.elements.display.seekTooltip)||0===this.duration)return;const s=this.elements.display.seekTooltip,n=`${this.config.classNames.tooltip}--visible`,a=e=>B(s,n,e);if(this.touch)return void a(!1);let l=0;const r=this.elements.progress.getBoundingClientRect();if(x.event(e)){const t=e.pageX-e.clientX;l=100/r.width*(e.pageX-r.left-t)}else{if(!W(s,n))return;l=Number.parseFloat(s.style.left,10)}l<0?l=0:l>100&&(l=100);const o=this.duration/100*l;s.textContent=xe.formatTime(o);const c=null===(t=this.config.markers)||void 0===t||null===(i=t.points)||void 0===i?void 0:i.find(({time:e})=>e===Math.round(o));c&&s.insertAdjacentHTML("afterbegin",`${c.label}<br>`),s.style.left=`${l}%`,x.event(e)&&["mouseenter","mouseleave"].includes(e.type)&&a("mouseenter"===e.type)},timeUpdate(e){const t=!x.element(this.elements.display.duration)&&this.config.invertTime;xe.updateTimeDisplay.call(this,this.elements.display.currentTime,t?this.duration-this.currentTime:this.currentTime,t),e&&"timeupdate"===e.type&&this.media.seeking||xe.updateProgress.call(this,e)},durationUpdate(){if(!this.supported.ui||!this.config.invertTime&&this.currentTime)return;if(this.duration>=2**32)return U(this.elements.display.currentTime,!0),void U(this.elements.progress,!0);x.element(this.elements.inputs.seek)&&this.elements.inputs.seek.setAttribute("aria-valuemax",this.duration);const e=x.element(this.elements.display.duration);!e&&this.config.displayDuration&&this.paused&&xe.updateTimeDisplay.call(this,this.elements.display.currentTime,this.duration),e&&xe.updateTimeDisplay.call(this,this.elements.display.duration,this.duration),this.config.markers.enabled&&xe.setMarkers.call(this),xe.updateSeekTooltip.call(this)},toggleMenuButton(e,t){U(this.elements.settings.buttons[e],!t)},updateSetting(e,t,i){const s=this.elements.settings.panels[e];let n=null,a=t;if("captions"===e)n=this.currentTrack;else{if(n=x.empty(i)?this[e]:i,x.empty(n)&&(n=this.config[e].default),!x.empty(this.options[e])&&!this.options[e].includes(n))return void this.debug.warn(`Unsupported value of '${n}' for ${e}`);if(!this.config[e].options.includes(n))return void this.debug.warn(`Disabled value of '${n}' for ${e}`)}if(x.element(a)||(a=s&&s.querySelector('[role="menu"]')),!x.element(a))return;this.elements.settings.buttons[e].querySelector(`.${this.config.classNames.menu.value}`).innerHTML=xe.getLabel.call(this,e,n);const l=a&&a.querySelector(`[value="${n}"]`);x.element(l)&&(l.checked=!0)},getLabel(e,t){switch(e){case"speed":return 1===t?Ae.get("normal",this.config):`${t}&times;`;case"quality":if(x.number(t)){const e=Ae.get(`qualityLabel.${t}`,this.config);return e.length?e:`${t}p`}return we(t);case"captions":return $e.getLabel.call(this);default:return null}},setQualityMenu(e){if(!x.element(this.elements.settings.panels.quality))return;const t="quality",i=this.elements.settings.panels.quality.querySelector('[role="menu"]');x.array(e)&&(this.options.quality=re(e).filter(e=>this.config.quality.options.includes(e)));const s=!x.empty(this.options.quality)&&this.options.quality.length>1;if(xe.toggleMenuButton.call(this,t,s),R(i),xe.checkMenu.call(this),!s)return;const n=e=>{const t=Ae.get(`qualityBadge.${e}`,this.config);return t.length?xe.createBadge.call(this,t):null};this.options.quality.sort((e,t)=>{const i=this.config.quality.options;return i.indexOf(e)>i.indexOf(t)?1:-1}).forEach(e=>{xe.createMenuItem.call(this,{value:e,list:i,type:t,title:xe.getLabel.call(this,"quality",e),badge:n(e)})}),xe.updateSetting.call(this,t,i)},setCaptionsMenu(){if(!x.element(this.elements.settings.panels.captions))return;const e="captions",t=this.elements.settings.panels.captions.querySelector('[role="menu"]'),i=$e.getTracks.call(this),s=Boolean(i.length);if(xe.toggleMenuButton.call(this,e,s),R(t),xe.checkMenu.call(this),!s)return;const n=i.map((e,i)=>({value:i,checked:this.captions.toggled&&this.currentTrack===i,title:$e.getLabel.call(this,e),badge:e.language&&xe.createBadge.call(this,e.language.toUpperCase()),list:t,type:"language"}));n.unshift({value:-1,checked:!this.captions.toggled,title:Ae.get("disabled",this.config),list:t,type:"language"}),n.forEach(xe.createMenuItem.bind(this)),xe.updateSetting.call(this,e,t)},setSpeedMenu(){if(!x.element(this.elements.settings.panels.speed))return;const e="speed",t=this.elements.settings.panels.speed.querySelector('[role="menu"]');this.options.speed=this.options.speed.filter(e=>e>=this.minimumSpeed&&e<=this.maximumSpeed);const i=!x.empty(this.options.speed)&&this.options.speed.length>1;xe.toggleMenuButton.call(this,e,i),R(t),xe.checkMenu.call(this),i&&(this.options.speed.forEach(i=>{xe.createMenuItem.call(this,{value:i,list:t,type:e,title:xe.getLabel.call(this,"speed",i)})}),xe.updateSetting.call(this,e,t))},checkMenu(){const{buttons:e}=this.elements.settings,t=!x.empty(e)&&Object.values(e).some(e=>!e.hidden);U(this.elements.settings.menu,!t)},focusFirstMenuItem(e,t=!1){if(this.elements.settings.popup.hidden)return;let i=e;x.element(i)||(i=Object.values(this.elements.settings.panels).find(e=>!e.hidden));const s=i.querySelector('[role^="menuitem"]');X.call(this,s,t)},toggleMenu(e){const{popup:t}=this.elements.settings,i=this.elements.buttons.settings;if(!x.element(t)||!x.element(i))return;const{hidden:s}=t;let n=s;if(x.boolean(e))n=e;else if(x.keyboardEvent(e)&&"Escape"===e.key)n=!1;else if(x.event(e)){const s=x.function(e.composedPath)?e.composedPath()[0]:e.target,a=t.contains(s);if(a||!a&&e.target!==i&&n)return}i.setAttribute("aria-expanded",n),U(t,!n),B(this.elements.container,this.config.classNames.menu.open,n),n&&x.keyboardEvent(e)?xe.focusFirstMenuItem.call(this,null,!0):n||s||X.call(this,i,x.keyboardEvent(e))},getMenuSize(e){const t=e.cloneNode(!0);t.style.position="absolute",t.style.opacity=0,t.removeAttribute("hidden"),e.parentNode.appendChild(t);const i=t.scrollWidth,s=t.scrollHeight;return H(t),{width:i,height:s}},showMenuPanel(e="",t=!1){const i=this.elements.container.querySelector(`#plyr-settings-${this.id}-${e}`);if(!x.element(i))return;const s=i.parentNode,n=Array.from(s.children).find(e=>!e.hidden);if(J.transitions&&!J.reducedMotion){s.style.width=`${n.scrollWidth}px`,s.style.height=`${n.scrollHeight}px`;const e=xe.getMenuSize.call(this,i),t=e=>{e.target===s&&["width","height"].includes(e.propertyName)&&(s.style.width="",s.style.height="",te.call(this,s,L,t))};ee.call(this,s,L,t),s.style.width=`${e.width}px`,s.style.height=`${e.height}px`}U(n,!0),U(i,!1),xe.focusFirstMenuItem.call(this,i,t)},setDownloadUrl(){const e=this.elements.buttons.download;x.element(e)&&e.setAttribute("href",this.download)},create(e){const{bindMenuItemShortcuts:t,createButton:i,createProgress:s,createRange:n,createTime:a,setQualityMenu:l,setSpeedMenu:r,showMenuPanel:o}=xe;this.elements.controls=null,x.array(this.config.controls)&&this.config.controls.includes("play-large")&&this.elements.container.appendChild(i.call(this,"play-large"));const c=q("div",V(this.config.selectors.controls.wrapper));this.elements.controls=c;const u={class:"plyr__controls__item"};return re(x.array(this.config.controls)?this.config.controls:[]).forEach(l=>{if("restart"===l&&c.appendChild(i.call(this,"restart",u)),"rewind"===l&&c.appendChild(i.call(this,"rewind",u)),"play"===l&&c.appendChild(i.call(this,"play",u)),"fast-forward"===l&&c.appendChild(i.call(this,"fast-forward",u)),"progress"===l){const t=q("div",{class:`${u.class} plyr__progress__container`}),i=q("div",V(this.config.selectors.progress));if(i.appendChild(n.call(this,"seek",{id:`plyr-seek-${e.id}`})),i.appendChild(s.call(this,"buffer")),this.config.tooltips.seek){const e=q("span",{class:this.config.classNames.tooltip},"00:00");i.appendChild(e),this.elements.display.seekTooltip=e}this.elements.progress=i,t.appendChild(this.elements.progress),c.appendChild(t)}if("current-time"===l&&c.appendChild(a.call(this,"currentTime",u)),"duration"===l&&c.appendChild(a.call(this,"duration",u)),"mute"===l||"volume"===l){let{volume:t}=this.elements;if(x.element(t)&&c.contains(t)||(t=q("div",_({},u,{class:`${u.class} plyr__volume`.trim()})),this.elements.volume=t,c.appendChild(t)),"mute"===l&&t.appendChild(i.call(this,"mute")),"volume"===l&&!ye.isIos&&!ye.isIPadOS){const i={max:1,step:.05,value:this.config.volume};t.appendChild(n.call(this,"volume",_(i,{id:`plyr-volume-${e.id}`})))}}if("captions"===l&&c.appendChild(i.call(this,"captions",u)),"settings"===l&&!x.empty(this.config.settings)){const s=q("div",_({},u,{class:`${u.class} plyr__menu`.trim(),hidden:""}));s.appendChild(i.call(this,"settings",{"aria-haspopup":!0,"aria-controls":`plyr-settings-${e.id}`,"aria-expanded":!1}));const n=q("div",{class:"plyr__menu__container",id:`plyr-settings-${e.id}`,hidden:""}),a=q("div"),l=q("div",{id:`plyr-settings-${e.id}-home`}),r=q("div",{role:"menu"});l.appendChild(r),a.appendChild(l),this.elements.settings.panels.home=l,this.config.settings.forEach(i=>{const s=q("button",_(V(this.config.selectors.buttons.settings),{type:"button",class:`${this.config.classNames.control} ${this.config.classNames.control}--forward`,role:"menuitem","aria-haspopup":!0,hidden:""}));t.call(this,s,i),ee.call(this,s,"click",()=>{o.call(this,i,!1)});const n=q("span",null,Ae.get(i,this.config)),l=q("span",{class:this.config.classNames.menu.value});l.innerHTML=e[i],n.appendChild(l),s.appendChild(n),r.appendChild(s);const c=q("div",{id:`plyr-settings-${e.id}-${i}`,hidden:""}),u=q("button",{type:"button",class:`${this.config.classNames.control} ${this.config.classNames.control}--back`});u.appendChild(q("span",{"aria-hidden":!0},Ae.get(i,this.config))),u.appendChild(q("span",{class:this.config.classNames.hidden},Ae.get("menuBack",this.config))),ee.call(this,c,"keydown",e=>{"ArrowLeft"===e.key&&(e.preventDefault(),e.stopPropagation(),o.call(this,"home",!0))},!1),ee.call(this,u,"click",()=>{o.call(this,"home",!1)}),c.appendChild(u),c.appendChild(q("div",{role:"menu"})),a.appendChild(c),this.elements.settings.buttons[i]=s,this.elements.settings.panels[i]=c}),n.appendChild(a),s.appendChild(n),c.appendChild(s),this.elements.settings.popup=n,this.elements.settings.menu=s}if("pip"===l&&J.pip&&c.appendChild(i.call(this,"pip",u)),"airplay"===l&&J.airplay&&c.appendChild(i.call(this,"airplay",u)),"download"===l){const e=_({},u,{element:"a",href:this.download,target:"_blank"});this.isHTML5&&(e.download="");const{download:t}=this.config.urls;!x.url(t)&&this.isEmbed&&_(e,{icon:`logo-${this.provider}`,label:this.provider}),c.appendChild(i.call(this,"download",e))}"fullscreen"===l&&c.appendChild(i.call(this,"fullscreen",u))}),this.isHTML5&&l.call(this,fe.getQualityOptions.call(this)),r.call(this),c},inject(){if(this.config.loadSprite){const e=xe.getIconUrl.call(this);e.cors&&Pe(e.url,"sprite-plyr")}this.id=Math.floor(1e4*Math.random());let e=null;this.elements.controls=null;const t={id:this.id,seektime:this.config.seekTime,title:this.config.title};let i=!0;x.function(this.config.controls)&&(this.config.controls=this.config.controls.call(this,t)),this.config.controls||(this.config.controls=[]),x.element(this.config.controls)||x.string(this.config.controls)?e=this.config.controls:(e=xe.create.call(this,{id:this.id,seektime:this.config.seekTime,speed:this.speed,quality:this.quality,captions:$e.getLabel.call(this)}),i=!1);let s;i&&x.string(this.config.controls)&&(e=(e=>{let i=e;return Object.entries(t).forEach(([e,t])=>{i=ve(i,`{${e}}`,t)}),i})(e)),x.string(this.config.selectors.controls.container)&&(s=document.querySelector(this.config.selectors.controls.container)),x.element(s)||(s=this.elements.container);if(s[x.element(e)?"insertAdjacentElement":"insertAdjacentHTML"]("afterbegin",e),x.element(this.elements.controls)||xe.findElements.call(this),!x.empty(this.elements.buttons)){const e=e=>{const t=this.config.classNames.controlPressed;e.setAttribute("aria-pressed","false"),Object.defineProperty(e,"pressed",{configurable:!0,enumerable:!0,get:()=>W(e,t),set(i=!1){B(e,t,i),e.setAttribute("aria-pressed",i?"true":"false")}})};Object.values(this.elements.buttons).filter(Boolean).forEach(t=>{x.array(t)||x.nodeList(t)?Array.from(t).filter(Boolean).forEach(e):e(t)})}if(ye.isEdge&&I(s),this.config.tooltips.controls){const{classNames:e,selectors:t}=this.config,i=`${t.controls.wrapper} ${t.labels} .${e.hidden}`,s=K.call(this,i);Array.from(s).forEach(e=>{B(e,this.config.classNames.hidden,!1),B(e,this.config.classNames.tooltip,!0)})}},setMediaMetadata(){try{"mediaSession"in navigator&&(navigator.mediaSession.metadata=new window.MediaMetadata({title:this.config.mediaMetadata.title,artist:this.config.mediaMetadata.artist,album:this.config.mediaMetadata.album,artwork:this.config.mediaMetadata.artwork}))}catch{}},setMarkers(){var e,t;if(!this.duration||this.elements.markers)return;const i=null===(e=this.config.markers)||void 0===e||null===(t=e.points)||void 0===t?void 0:t.filter(({time:e})=>e>0&&e<this.duration);if(null==i||!i.length)return;const s=document.createDocumentFragment(),n=document.createDocumentFragment();let a=null;const l=`${this.config.classNames.tooltip}--visible`,r=e=>B(a,l,e);i.forEach(e=>{const t=q("span",{class:this.config.classNames.marker},""),i=e.time/this.duration*100+"%";a&&(t.addEventListener("mouseenter",()=>{e.label||(a.style.left=i,a.innerHTML=e.label,r(!0))}),t.addEventListener("mouseleave",()=>{r(!1)})),t.addEventListener("click",()=>{this.currentTime=e.time}),t.style.left=i,n.appendChild(t)}),s.appendChild(n),this.config.tooltips.seek||(a=q("span",{class:this.config.classNames.tooltip},""),s.appendChild(a)),this.elements.markers={points:n,tip:a},this.elements.progress.appendChild(s)}};function Le(e,t=!0){let i=e;if(t){const e=document.createElement("a");e.href=i,i=e.href}try{return new URL(i)}catch{return null}}function Ie(e){const t=new URLSearchParams;return x.object(e)&&Object.entries(e).forEach(([e,i])=>{t.set(e,i)}),t}const $e={setup(){if(!this.supported.ui)return;if(!this.isVideo||this.isYouTube||this.isHTML5&&!J.textTracks)return void(x.array(this.config.controls)&&this.config.controls.includes("settings")&&this.config.settings.includes("captions")&&xe.setCaptionsMenu.call(this));var e,t;if(x.element(this.elements.captions)||(this.elements.captions=q("div",V(this.config.selectors.captions)),this.elements.captions.setAttribute("dir","auto"),e=this.elements.captions,t=this.elements.wrapper,x.element(e)&&x.element(t)&&t.parentNode.insertBefore(e,t.nextSibling)),ye.isIE&&window.URL){const e=this.media.querySelectorAll("track");Array.from(e).forEach(e=>{const t=e.getAttribute("src"),i=Le(t);null!==i&&i.hostname!==window.location.href.hostname&&["http:","https:"].includes(i.protocol)&&Ee(t,"blob").then(t=>{e.setAttribute("src",window.URL.createObjectURL(t))}).catch(()=>{H(e)})})}const i=re((navigator.languages||[navigator.language||navigator.userLanguage||"en"]).map(e=>e.split("-")[0]));let s=(this.storage.get("language")||this.captions.language||this.config.captions.language||"auto").toLowerCase();"auto"===s&&([s]=i);let n=this.storage.get("captions")||this.captions.active;if(x.boolean(n)||({active:n}=this.config.captions),Object.assign(this.captions,{toggled:!1,active:n,language:s,languages:i}),this.isHTML5){const e=this.config.captions.update?"addtrack removetrack":"removetrack";ee.call(this,this.media.textTracks,e,$e.update.bind(this))}setTimeout($e.update.bind(this),0)},update(){const e=$e.getTracks.call(this,!0),{active:t,language:i,meta:s,currentTrackNode:n}=this.captions,a=Boolean(e.find(e=>e.language===i));this.isHTML5&&this.isVideo&&e.filter(e=>!s.get(e)).forEach(e=>{this.debug.log("Track added",e),s.set(e,{default:"showing"===e.mode}),"showing"===e.mode&&(e.mode="hidden"),ee.call(this,e,"cuechange",()=>$e.updateCues.call(this))}),(a&&this.language!==i||!e.includes(n))&&($e.setLanguage.call(this,i),$e.toggle.call(this,t&&a)),this.elements&&B(this.elements.container,this.config.classNames.captions.enabled,!x.empty(e)),x.array(this.config.controls)&&this.config.controls.includes("settings")&&this.config.settings.includes("captions")&&xe.setCaptionsMenu.call(this)},toggle(e,t=!0){if(!this.supported.ui)return;const{toggled:i}=this.captions,s=this.config.classNames.captions.active,n=x.nullOrUndefined(e)?!i:e;if(n!==i){if(t||(this.captions.active=n,this.storage.set({captions:n})),!this.language&&n&&!t){const e=$e.getTracks.call(this),t=$e.findTrack.call(this,[this.captions.language,...this.captions.languages],!0);return this.captions.language=t.language,void $e.set.call(this,e.indexOf(t))}this.elements.buttons.captions&&(this.elements.buttons.captions.pressed=n),B(this.elements.container,s,n),this.captions.toggled=n,xe.updateSetting.call(this,"captions"),se.call(this,this.media,n?"captionsenabled":"captionsdisabled")}setTimeout(()=>{n&&this.captions.toggled&&(this.captions.currentTrackNode.mode="hidden")})},set(e,t=!0){const i=$e.getTracks.call(this);if(-1!==e)if(x.number(e))if(e in i){if(this.captions.currentTrack!==e){this.captions.currentTrack=e;const s=i[e],{language:n}=s||{};this.captions.currentTrackNode=s,xe.updateSetting.call(this,"captions"),t||(this.captions.language=n,this.storage.set({language:n})),this.isVimeo&&this.embed.enableTextTrack(n,null,!1),se.call(this,this.media,"languagechange")}$e.toggle.call(this,!0,t),this.isHTML5&&this.isVideo&&$e.updateCues.call(this)}else this.debug.warn("Track not found",e);else this.debug.warn("Invalid caption argument",e);else $e.toggle.call(this,!1,t)},setLanguage(e,t=!0){if(!x.string(e))return void this.debug.warn("Invalid language argument",e);const i=e.toLowerCase();this.captions.language=i;const s=$e.getTracks.call(this),n=$e.findTrack.call(this,[i]);$e.set.call(this,s.indexOf(n),t)},getTracks(e=!1){return Array.from((this.media||{}).textTracks||[]).filter(t=>!this.isHTML5||e||this.captions.meta.has(t)).filter(e=>["captions","subtitles"].includes(e.kind))},findTrack(e,t=!1){const i=$e.getTracks.call(this),s=e=>Number((this.captions.meta.get(e)||{}).default),n=Array.from(i).sort((e,t)=>s(t)-s(e));let a;return e.every(e=>(a=n.find(t=>t.language===e),!a)),a||(t?n[0]:void 0)},getCurrentTrack(){return $e.getTracks.call(this)[this.currentTrack]},getLabel(e){let t=e;return!x.track(t)&&J.textTracks&&this.captions.toggled&&(t=$e.getCurrentTrack.call(this)),x.track(t)?x.empty(t.label)?x.empty(t.language)?Ae.get("enabled",this.config):e.language.toUpperCase():t.label:Ae.get("disabled",this.config)},updateCues(e){if(!this.supported.ui)return;if(!x.element(this.elements.captions))return void this.debug.warn("No captions element to render to");if(!x.nullOrUndefined(e)&&!Array.isArray(e))return void this.debug.warn("updateCues: Invalid input",e);let t=e;if(!t){const e=$e.getCurrentTrack.call(this);t=Array.from((e||{}).activeCues||[]).map(e=>e.getCueAsHTML()).map(Te)}const i=t.map(e=>e.trim()).join("\n");if(i!==this.elements.captions.innerHTML){R(this.elements.captions);const e=q("span",V(this.config.selectors.caption));e.innerHTML=i,this.elements.captions.appendChild(e),se.call(this,this.media,"cuechange")}}},_e={enabled:!0,title:"",debug:!1,autoplay:!1,autopause:!0,playsinline:!0,seekTime:10,volume:1,muted:!1,duration:null,displayDuration:!0,invertTime:!0,toggleInvert:!0,ratio:null,clickToPlay:!0,hideControls:!0,resetOnEnd:!1,disableContextMenu:!0,loadSprite:!0,iconPrefix:"plyr",iconUrl:"https://cdn.plyr.io/3.8.4/plyr.svg",blankVideo:"https://cdn.plyr.io/static/blank.mp4",quality:{default:576,options:[4320,2880,2160,1440,1080,720,576,480,360,240],forced:!1,onChange:null},loop:{active:!1},speed:{selected:1,options:[.5,.75,1,1.25,1.5,1.75,2,4]},keyboard:{focused:!0,global:!1},tooltips:{controls:!1,seek:!0},captions:{active:!1,language:"auto",update:!1},fullscreen:{enabled:!0,fallback:!0,iosNative:!1},storage:{enabled:!0,key:"plyr"},controls:["play-large","play","progress","current-time","mute","volume","captions","settings","pip","airplay","fullscreen"],settings:["captions","quality","speed"],i18n:{restart:"Restart",rewind:"Rewind {seektime}s",play:"Play",pause:"Pause",fastForward:"Forward {seektime}s",seek:"Seek",seekLabel:"{currentTime} of {duration}",played:"Played",buffered:"Buffered",currentTime:"Current time",duration:"Duration",volume:"Volume",mute:"Mute",unmute:"Unmute",enableCaptions:"Enable captions",disableCaptions:"Disable captions",download:"Download",enterFullscreen:"Enter fullscreen",exitFullscreen:"Exit fullscreen",frameTitle:"Player for {title}",captions:"Captions",settings:"Settings",pip:"PIP",menuBack:"Go back to previous menu",speed:"Speed",normal:"Normal",quality:"Quality",loop:"Loop",start:"Start",end:"End",all:"All",reset:"Reset",disabled:"Disabled",enabled:"Enabled",advertisement:"Ad",qualityBadge:{2160:"4K",1440:"HD",1080:"HD",720:"HD",576:"SD",480:"SD"}},urls:{download:null,vimeo:{sdk:"https://player.vimeo.com/api/player.js",iframe:"https://player.vimeo.com/video/{0}?{1}",api:"https://vimeo.com/api/oembed.json?url={0}"},youtube:{sdk:"https://www.youtube.com/iframe_api",api:"https://noembed.com/embed?url=https://www.youtube.com/watch?v={0}"},googleIMA:{sdk:"https://imasdk.googleapis.com/js/sdkloader/ima3.js"}},listeners:{seek:null,play:null,pause:null,restart:null,rewind:null,fastForward:null,mute:null,volume:null,captions:null,download:null,fullscreen:null,pip:null,airplay:null,speed:null,quality:null,loop:null,language:null},events:["ended","progress","stalled","playing","waiting","canplay","canplaythrough","loadstart","loadeddata","loadedmetadata","timeupdate","volumechange","play","pause","error","seeking","seeked","emptied","ratechange","cuechange","download","enterfullscreen","exitfullscreen","captionsenabled","captionsdisabled","languagechange","controlshidden","controlsshown","ready","statechange","qualitychange","adsloaded","adscontentpause","adscontentresume","adstarted","adsmidpoint","adscomplete","adsallcomplete","adsimpression","adsclick"],selectors:{editable:"input, textarea, select, [contenteditable]",container:".plyr",controls:{container:null,wrapper:".plyr__controls"},labels:"[data-plyr]",buttons:{play:'[data-plyr="play"]',pause:'[data-plyr="pause"]',restart:'[data-plyr="restart"]',rewind:'[data-plyr="rewind"]',fastForward:'[data-plyr="fast-forward"]',mute:'[data-plyr="mute"]',captions:'[data-plyr="captions"]',download:'[data-plyr="download"]',fullscreen:'[data-plyr="fullscreen"]',pip:'[data-plyr="pip"]',airplay:'[data-plyr="airplay"]',settings:'[data-plyr="settings"]',loop:'[data-plyr="loop"]'},inputs:{seek:'[data-plyr="seek"]',volume:'[data-plyr="volume"]',speed:'[data-plyr="speed"]',language:'[data-plyr="language"]',quality:'[data-plyr="quality"]'},display:{currentTime:".plyr__time--current",duration:".plyr__time--duration",buffer:".plyr__progress__buffer",loop:".plyr__progress__loop",volume:".plyr__volume--display"},progress:".plyr__progress",captions:".plyr__captions",caption:".plyr__caption"},classNames:{type:"plyr--{0}",provider:"plyr--{0}",video:"plyr__video-wrapper",embed:"plyr__video-embed",videoFixedRatio:"plyr__video-wrapper--fixed-ratio",embedContainer:"plyr__video-embed__container",poster:"plyr__poster",posterEnabled:"plyr__poster-enabled",ads:"plyr__ads",control:"plyr__control",controlPressed:"plyr__control--pressed",playing:"plyr--playing",paused:"plyr--paused",stopped:"plyr--stopped",loading:"plyr--loading",hover:"plyr--hover",tooltip:"plyr__tooltip",cues:"plyr__cues",marker:"plyr__progress__marker",hidden:"plyr__sr-only",hideControls:"plyr--hide-controls",isTouch:"plyr--is-touch",uiSupported:"plyr--full-ui",noTransition:"plyr--no-transition",display:{time:"plyr__time"},menu:{value:"plyr__menu__value",badge:"plyr__badge",open:"plyr--menu-open"},captions:{enabled:"plyr--captions-enabled",active:"plyr--captions-active"},fullscreen:{enabled:"plyr--fullscreen-enabled",fallback:"plyr--fullscreen-fallback"},pip:{supported:"plyr--pip-supported",active:"plyr--pip-active"},airplay:{supported:"plyr--airplay-supported",active:"plyr--airplay-active"},previewThumbnails:{thumbContainer:"plyr__preview-thumb",thumbContainerShown:"plyr__preview-thumb--is-shown",imageContainer:"plyr__preview-thumb__image-container",timeContainer:"plyr__preview-thumb__time-container",scrubbingContainer:"plyr__preview-scrubbing",scrubbingContainerShown:"plyr__preview-scrubbing--is-shown"}},attributes:{embed:{provider:"data-plyr-provider",id:"data-plyr-embed-id",hash:"data-plyr-embed-hash"}},ads:{enabled:!1,publisherId:"",tagUrl:""},previewThumbnails:{enabled:!1,src:"",withCredentials:!1},vimeo:{byline:!1,portrait:!1,title:!1,speed:!0,transparent:!1,customControls:!0,referrerPolicy:null,premium:!1},youtube:{rel:0,showinfo:0,iv_load_policy:3,modestbranding:1,customControls:!0,noCookie:!1},mediaMetadata:{title:"",artist:"",album:"",artwork:[]},markers:{enabled:!1,points:[]}},Oe="picture-in-picture",je="inline",qe={html5:"html5",youtube:"youtube",vimeo:"vimeo"},De="audio",He="video";function Re(){}class Fe{constructor(e=!1){this.enabled=window.console&&e,this.enabled&&this.log("Debugging enabled")}get log(){return this.enabled?Function.prototype.bind.call(console.log,console):Re}get warn(){return this.enabled?Function.prototype.bind.call(console.warn,console):Re}get error(){return this.enabled?Function.prototype.bind.call(console.error,console):Re}}class Ve{constructor(t){e(this,"onChange",()=>{if(!this.supported)return;const e=this.player.elements.buttons.fullscreen;x.element(e)&&(e.pressed=this.active);const t=this.target===this.player.media?this.target:this.player.elements.container;se.call(this.player,t,this.active?"enterfullscreen":"exitfullscreen",!0)}),e(this,"toggleFallback",(e=!1)=>{var t,i;e?this.scrollPosition={x:null!==(t=window.scrollX)&&void 0!==t?t:0,y:null!==(i=window.scrollY)&&void 0!==i?i:0}:window.scrollTo(this.scrollPosition.x,this.scrollPosition.y);if(document.body.style.overflow=e?"hidden":"",B(this.target,this.player.config.classNames.fullscreen.fallback,e),ye.isIos){let t=document.head.querySelector('meta[name="viewport"]');const i="viewport-fit=cover";t||(t=document.createElement("meta"),t.setAttribute("name","viewport"));const s=x.string(t.content)&&t.content.includes(i);e?(this.cleanupViewport=!s,s||(t.content+=`,${i}`)):this.cleanupViewport&&(t.content=t.content.split(",").filter(e=>e.trim()!==i).join(","))}this.onChange()}),e(this,"trapFocus",e=>{if(ye.isIos||ye.isIPadOS||!this.active||"Tab"!==e.key)return;const t=document.activeElement,i=K.call(this.player,"a[href], button:not(:disabled), input:not(:disabled), [tabindex]"),[s]=i,n=i[i.length-1];t!==n||e.shiftKey?t===s&&e.shiftKey&&(n.focus(),e.preventDefault()):(s.focus(),e.preventDefault())}),e(this,"update",()=>{if(this.supported){let e;e=this.forceFallback?"Fallback (forced)":Ve.nativeSupported?"Native":"Fallback",this.player.debug.log(`${e} fullscreen enabled`)}else this.player.debug.log("Fullscreen not supported and fallback disabled");B(this.player.elements.container,this.player.config.classNames.fullscreen.enabled,this.supported)}),e(this,"enter",()=>{this.supported&&(ye.isIos&&this.player.config.fullscreen.iosNative?this.player.isVimeo?this.player.embed.requestFullscreen():this.target.webkitEnterFullscreen():!Ve.nativeSupported||this.forceFallback?this.toggleFallback(!0):this.prefix?x.empty(this.prefix)||this.target[`${this.prefix}Request${this.property}`]():this.target.requestFullscreen({navigationUI:"hide"}))}),e(this,"exit",()=>{if(this.supported)if(ye.isIos&&this.player.config.fullscreen.iosNative)this.player.isVimeo?this.player.embed.exitFullscreen():this.target.webkitEnterFullscreen(),le(this.player.play());else if(!Ve.nativeSupported||this.forceFallback)this.toggleFallback(!1);else if(this.prefix){if(!x.empty(this.prefix)){const e="moz"===this.prefix?"Cancel":"Exit";document[`${this.prefix}${e}${this.property}`]()}}else(document.cancelFullScreen||document.exitFullscreen).call(document)}),e(this,"toggle",()=>{this.active?this.exit():this.enter()}),this.player=t,this.prefix=Ve.prefix,this.property=Ve.property,this.scrollPosition={x:0,y:0},this.forceFallback="force"===t.config.fullscreen.fallback,this.player.elements.fullscreen=t.config.fullscreen.container&&function(e,t){const{prototype:i}=Element;return(i.closest||function(){let e=this;do{if(z.matches(e,t))return e;e=e.parentElement||e.parentNode}while(null!==e&&1===e.nodeType);return null}).call(e,t)}(this.player.elements.container,t.config.fullscreen.container),ee.call(this.player,document,"ms"===this.prefix?"MSFullscreenChange":`${this.prefix}fullscreenchange`,()=>{this.onChange()}),ee.call(this.player,this.player.elements.container,"dblclick",e=>{x.element(this.player.elements.controls)&&this.player.elements.controls.contains(e.target)||this.player.listeners.proxy(e,this.toggle,"fullscreen")}),ee.call(this,this.player.elements.container,"keydown",e=>this.trapFocus(e)),this.update()}static get nativeSupported(){return!!(document.fullscreenEnabled||document.webkitFullscreenEnabled||document.mozFullScreenEnabled||document.msFullscreenEnabled)}get useNative(){return Ve.nativeSupported&&!this.forceFallback}static get prefix(){if(x.function(document.exitFullscreen))return"";let e="";return["webkit","moz","ms"].some(t=>!(!x.function(document[`${t}ExitFullscreen`])&&!x.function(document[`${t}CancelFullScreen`]))&&(e=t,!0)),e}static get property(){return"moz"===this.prefix?"FullScreen":"Fullscreen"}get supported(){return[this.player.config.fullscreen.enabled,this.player.isVideo,Ve.nativeSupported||this.player.config.fullscreen.fallback,!this.player.isYouTube||Ve.nativeSupported||!ye.isIos||this.player.config.playsinline&&!this.player.config.fullscreen.iosNative].every(Boolean)}get active(){if(!this.supported)return!1;if(!Ve.nativeSupported||this.forceFallback)return W(this.target,this.player.config.classNames.fullscreen.fallback);const e=this.prefix?this.target.getRootNode()[`${this.prefix}${this.property}Element`]:this.target.getRootNode().fullscreenElement;return e&&e.shadowRoot?e===this.target.getRootNode().host:e===this.target}get target(){var e;return ye.isIos&&this.player.config.fullscreen.iosNative?this.player.media:null!==(e=this.player.elements.fullscreen)&&void 0!==e?e:this.player.elements.container}}function Ue(e,t=1){return new Promise((i,s)=>{const n=new Image,a=()=>{delete n.onload,delete n.onerror,(n.naturalWidth>=t?i:s)(n)};Object.assign(n,{onload:a,onerror:a,src:e})})}const Be={addStyleHook(){B(this.elements.container,this.config.selectors.container.replace(".",""),!0),B(this.elements.container,this.config.classNames.uiSupported,this.supported.ui)},toggleNativeControls(e=!1){e&&this.isHTML5?this.media.setAttribute("controls",""):this.media.removeAttribute("controls")},build(){if(this.listeners.media(),!this.supported.ui)return this.debug.warn(`Basic support only for ${this.provider} ${this.type}`),void Be.toggleNativeControls.call(this,!0);x.element(this.elements.controls)||(xe.inject.call(this),this.listeners.controls()),Be.toggleNativeControls.call(this),this.isHTML5&&$e.setup.call(this),this.volume=null,this.muted=null,this.loop=null,this.quality=null,this.speed=null,xe.updateVolume.call(this),xe.timeUpdate.call(this),xe.durationUpdate.call(this),Be.checkPlaying.call(this),B(this.elements.container,this.config.classNames.pip.supported,J.pip&&this.isHTML5&&this.isVideo),B(this.elements.container,this.config.classNames.airplay.supported,J.airplay&&this.isHTML5),B(this.elements.container,this.config.classNames.isTouch,this.touch),this.ready=!0,setTimeout(()=>{se.call(this,this.media,"ready")},0),Be.setTitle.call(this),this.poster&&Be.setPoster.call(this,this.poster,!1).catch(()=>{}),this.config.duration&&xe.durationUpdate.call(this),this.config.mediaMetadata&&xe.setMediaMetadata.call(this)},setTitle(){let e=Ae.get("play",this.config);if(x.string(this.config.title)&&!x.empty(this.config.title)&&(e+=`, ${this.config.title}`),Array.from(this.elements.buttons.play||[]).forEach(t=>{t.setAttribute("aria-label",e)}),this.isEmbed){const e=Y.call(this,"iframe");if(!x.element(e))return;const t=x.empty(this.config.title)?"video":this.config.title,i=Ae.get("frameTitle",this.config);e.setAttribute("title",i.replace("{title}",t))}},togglePoster(e){B(this.elements.container,this.config.classNames.posterEnabled,e)},setPoster(e,t=!0){return t&&this.poster?Promise.reject(new Error("Poster already set")):(this.media.setAttribute("data-poster",e),this.elements.poster.removeAttribute("hidden"),ae.call(this).then(()=>Ue(e)).catch(t=>{throw e===this.poster&&Be.togglePoster.call(this,!1),t}).then(()=>{if(e!==this.poster)throw new Error("setPoster cancelled by later call to setPoster")}).then(()=>(Object.assign(this.elements.poster.style,{backgroundImage:`url('${e}')`,backgroundSize:""}),Be.togglePoster.call(this,!0),e)))},checkPlaying(e){B(this.elements.container,this.config.classNames.playing,this.playing),B(this.elements.container,this.config.classNames.paused,this.paused),B(this.elements.container,this.config.classNames.stopped,this.stopped),Array.from(this.elements.buttons.play||[]).forEach(e=>{Object.assign(e,{pressed:this.playing}),e.setAttribute("aria-label",Ae.get(this.playing?"pause":"play",this.config))}),x.event(e)&&"timeupdate"===e.type||Be.toggleControls.call(this)},checkLoading(e){this.loading=["stalled","waiting"].includes(e.type),clearTimeout(this.timers.loading),this.timers.loading=setTimeout(()=>{B(this.elements.container,this.config.classNames.loading,this.loading),Be.toggleControls.call(this)},this.loading?250:0)},toggleControls(e){const{controls:t}=this.elements;if(t&&this.config.hideControls){const i=this.touch&&this.lastSeekTime+2e3>Date.now();this.toggleControls(Boolean(e||this.loading||this.paused||t.pressed||t.hover||i))}},migrateStyles(){Object.values({...this.media.style}).filter(e=>!x.empty(e)&&x.string(e)&&e.startsWith("--plyr")).forEach(e=>{this.elements.container.style.setProperty(e,this.media.style.getPropertyValue(e)),this.media.style.removeProperty(e)}),x.empty(this.media.style)&&this.media.removeAttribute("style")}};class We{constructor(t){e(this,"firstTouch",()=>{const{player:e}=this,{elements:t}=e;e.touch=!0,B(t.container,e.config.classNames.isTouch,!0)}),e(this,"global",(e=!0)=>{const{player:t}=this;t.config.keyboard.global&&Z.call(t,window,"keydown keyup",this.handleKey,e,!1),Z.call(t,document.body,"click",this.toggleMenu,e),ie.call(t,document.body,"touchstart",this.firstTouch)}),e(this,"container",()=>{const{player:e}=this,{config:t,elements:i,timers:s}=e;!t.keyboard.global&&t.keyboard.focused&&ee.call(e,i.container,"keydown keyup",this.handleKey,!1),ee.call(e,i.container,"mousemove mouseleave touchstart touchmove enterfullscreen exitfullscreen",t=>{const{controls:n}=i;n&&"enterfullscreen"===t.type&&(n.pressed=!1,n.hover=!1);let a=0;["touchstart","touchmove","mousemove"].includes(t.type)&&(Be.toggleControls.call(e,!0),a=e.touch?3e3:2e3),clearTimeout(s.controls),s.controls=setTimeout(()=>Be.toggleControls.call(e,!1),a)});const n=()=>{if(!e.isVimeo||e.config.vimeo.premium)return;const t=i.wrapper,{active:s}=e.fullscreen,[n,a]=me.call(e),l=ce(`aspect-ratio: ${n} / ${a}`);if(!s)return void(l?(t.style.width=null,t.style.height=null):(t.style.maxWidth=null,t.style.margin=null));const[r,o]=[Math.max(document.documentElement.clientWidth||0,window.innerWidth||0),Math.max(document.documentElement.clientHeight||0,window.innerHeight||0)],c=r/o>n/a;l?(t.style.width=c?"auto":"100%",t.style.height=c?"100%":"auto"):(t.style.maxWidth=c?o/a*n+"px":null,t.style.margin=c?"0 auto":null)},a=()=>{clearTimeout(s.resized),s.resized=setTimeout(n,50)};ee.call(e,i.container,"enterfullscreen exitfullscreen",t=>{const{target:s}=e.fullscreen;if(s!==i.container)return;if(!e.isEmbed&&x.empty(e.config.ratio))return;n();("enterfullscreen"===t.type?ee:te).call(e,window,"resize",a)})}),e(this,"media",()=>{const{player:e}=this,{elements:t}=e;if(ee.call(e,e.media,"timeupdate seeking seeked",t=>xe.timeUpdate.call(e,t)),ee.call(e,e.media,"durationchange loadeddata loadedmetadata",t=>xe.durationUpdate.call(e,t)),ee.call(e,e.media,"ended",()=>{e.isHTML5&&e.isVideo&&e.config.resetOnEnd&&(e.restart(),e.pause())}),ee.call(e,e.media,"progress playing seeking seeked",t=>xe.updateProgress.call(e,t)),ee.call(e,e.media,"volumechange",t=>xe.updateVolume.call(e,t)),ee.call(e,e.media,"playing play pause ended emptied timeupdate",t=>Be.checkPlaying.call(e,t)),ee.call(e,e.media,"waiting canplay seeked playing",t=>Be.checkLoading.call(e,t)),e.supported.ui&&e.config.clickToPlay&&!e.isAudio){const i=Y.call(e,`.${e.config.classNames.video}`);if(!x.element(i))return;ee.call(e,t.container,"click",s=>{([t.container,i].includes(s.target)||i.contains(s.target))&&(e.touch&&e.config.hideControls||(e.ended?(this.proxy(s,e.restart,"restart"),this.proxy(s,()=>{le(e.play())},"play")):this.proxy(s,()=>{le(e.togglePlay())},"play")))})}e.supported.ui&&e.config.disableContextMenu&&ee.call(e,t.wrapper,"contextmenu",e=>{e.preventDefault()},!1),ee.call(e,e.media,"volumechange",()=>{e.storage.set({volume:e.volume,muted:e.muted})}),ee.call(e,e.media,"ratechange",()=>{xe.updateSetting.call(e,"speed"),e.storage.set({speed:e.speed})}),ee.call(e,e.media,"qualitychange",t=>{xe.updateSetting.call(e,"quality",null,t.detail.quality)}),ee.call(e,e.media,"ready qualitychange",()=>{xe.setDownloadUrl.call(e)});const i=e.config.events.concat(["keyup","keydown"]).join(" ");ee.call(e,e.media,i,i=>{let{detail:s={}}=i;"error"===i.type&&(s=e.media.error),se.call(e,t.container,i.type,!0,s)})}),e(this,"proxy",(e,t,i)=>{const{player:s}=this,n=s.config.listeners[i];let a=!0;x.function(n)&&(a=n.call(s,e)),!1!==a&&x.function(t)&&t.call(s,e)}),e(this,"bind",(e,t,i,s,n=!0)=>{const{player:a}=this,l=a.config.listeners[s],r=x.function(l);ee.call(a,e,t,e=>this.proxy(e,i,s),n&&!r)}),e(this,"controls",()=>{const{player:e}=this,{elements:t}=e,i=ye.isIE?"change":"input";if(t.buttons.play&&Array.from(t.buttons.play).forEach(t=>{this.bind(t,"click",()=>{le(e.togglePlay())},"play")}),this.bind(t.buttons.restart,"click",e.restart,"restart"),this.bind(t.buttons.rewind,"click",()=>{e.lastSeekTime=Date.now(),e.rewind()},"rewind"),this.bind(t.buttons.fastForward,"click",()=>{e.lastSeekTime=Date.now(),e.forward()},"fastForward"),this.bind(t.buttons.mute,"click",()=>{e.muted=!e.muted},"mute"),this.bind(t.buttons.captions,"click",()=>e.toggleCaptions()),this.bind(t.buttons.download,"click",()=>{se.call(e,e.media,"download")},"download"),this.bind(t.buttons.fullscreen,"click",()=>{e.fullscreen.toggle()},"fullscreen"),this.bind(t.buttons.pip,"click",()=>{e.pip="toggle"},"pip"),this.bind(t.buttons.airplay,"click",e.airplay,"airplay"),this.bind(t.buttons.settings,"click",t=>{t.stopPropagation(),t.preventDefault(),xe.toggleMenu.call(e,t)},null,!1),this.bind(t.buttons.settings,"keyup",t=>{[" ","Enter"].includes(t.key)&&("Enter"!==t.key?(t.preventDefault(),t.stopPropagation(),xe.toggleMenu.call(e,t)):xe.focusFirstMenuItem.call(e,null,!0))},null,!1),this.bind(t.settings.menu,"keydown",t=>{"Escape"===t.key&&xe.toggleMenu.call(e,t)}),this.bind(t.inputs.seek,"mousedown mousemove",e=>{const i=t.progress.getBoundingClientRect(),s=e.pageX-e.clientX,n=100/i.width*(e.pageX-i.left-s);e.currentTarget.setAttribute("seek-value",n)}),this.bind(t.inputs.seek,"mousedown mouseup keydown keyup touchstart touchend",t=>{const i=t.currentTarget,s="play-on-seeked";if(x.keyboardEvent(t)&&!["ArrowLeft","ArrowRight"].includes(t.key))return;e.lastSeekTime=Date.now();const n=i.hasAttribute(s),a=["mouseup","touchend","keyup"].includes(t.type);n&&a?(i.removeAttribute(s),le(e.play())):!a&&e.playing&&(i.setAttribute(s,""),e.pause())}),ye.isIos){const t=K.call(e,'input[type="range"]');Array.from(t).forEach(e=>this.bind(e,i,e=>I(e.target)))}this.bind(t.inputs.seek,i,t=>{const i=t.currentTarget;let s=i.getAttribute("seek-value");x.empty(s)&&(s=i.value),i.removeAttribute("seek-value"),e.currentTime=s/i.max*e.duration},"seek"),this.bind(t.progress,"mouseenter mouseleave mousemove",t=>xe.updateSeekTooltip.call(e,t)),this.bind(t.progress,"mousemove touchmove",t=>{const{previewThumbnails:i}=e;i&&i.loaded&&i.startMove(t)}),this.bind(t.progress,"mouseleave touchend click",()=>{const{previewThumbnails:t}=e;t&&t.loaded&&t.endMove(!1,!0)}),this.bind(t.progress,"mousedown touchstart",t=>{const{previewThumbnails:i}=e;i&&i.loaded&&i.startScrubbing(t)}),this.bind(t.progress,"mouseup touchend",t=>{const{previewThumbnails:i}=e;i&&i.loaded&&i.endScrubbing(t)}),ye.isWebKit&&Array.from(K.call(e,'input[type="range"]')).forEach(t=>{this.bind(t,"input",t=>xe.updateRangeFill.call(e,t.target))}),e.config.toggleInvert&&!x.element(t.display.duration)&&this.bind(t.display.currentTime,"click",()=>{0!==e.currentTime&&(e.config.invertTime=!e.config.invertTime,xe.timeUpdate.call(e))}),this.bind(t.inputs.volume,i,t=>{e.volume=t.target.value},"volume"),this.bind(t.controls,"mouseenter mouseleave",i=>{t.controls.hover=!e.touch&&"mouseenter"===i.type}),t.fullscreen&&Array.from(t.fullscreen.children).filter(e=>!e.contains(t.container)).forEach(i=>{this.bind(i,"mouseenter mouseleave",i=>{t.controls&&(t.controls.hover=!e.touch&&"mouseenter"===i.type)})}),this.bind(t.controls,"mousedown mouseup touchstart touchend touchcancel",e=>{t.controls.pressed=["mousedown","touchstart"].includes(e.type)}),this.bind(t.controls,"focusin",()=>{const{config:i,timers:s}=e;B(t.controls,i.classNames.noTransition,!0),Be.toggleControls.call(e,!0),setTimeout(()=>{B(t.controls,i.classNames.noTransition,!1)},0);const n=this.touch?3e3:4e3;clearTimeout(s.controls),s.controls=setTimeout(()=>Be.toggleControls.call(e,!1),n)}),this.bind(t.inputs.volume,"wheel",t=>{const i=t.webkitDirectionInvertedFromDevice,[s,n]=[t.deltaX,-t.deltaY].map(e=>i?-e:e),a=Math.sign(Math.abs(s)>Math.abs(n)?s:n);e.increaseVolume(a/50);const{volume:l}=e.media;(1===a&&l<1||-1===a&&l>0)&&t.preventDefault()},"volume",!1)}),this.player=t,this.lastKey=null,this.focusTimer=null,this.lastKeyDown=null,this.handleKey=this.handleKey.bind(this),this.toggleMenu=this.toggleMenu.bind(this),this.firstTouch=this.firstTouch.bind(this)}handleKey(e){const{player:t}=this,{elements:i}=t,{key:s,type:n,altKey:a,ctrlKey:l,metaKey:r,shiftKey:o}=e,c="keydown"===n,u=c&&s===this.lastKey;if(a||l||r||o)return;if(!s)return;if(c){const n=document.activeElement;if(x.element(n)){const{editable:s}=t.config.selectors,{seek:a}=i.inputs;if(n!==a&&z(n,s))return;if(" "===e.key&&z(n,'button, [role^="menuitem"]'))return}switch([" ","ArrowLeft","ArrowUp","ArrowRight","ArrowDown","0","1","2","3","4","5","6","7","8","9","c","f","k","l","m"].includes(s)&&(e.preventDefault(),e.stopPropagation()),s){case"0":case"1":case"2":case"3":case"4":case"5":case"6":case"7":case"8":case"9":u||(h=Number.parseInt(s,10),t.currentTime=t.duration/10*h);break;case" ":case"k":u||le(t.togglePlay());break;case"ArrowUp":t.increaseVolume(.1);break;case"ArrowDown":t.decreaseVolume(.1);break;case"m":u||(t.muted=!t.muted);break;case"ArrowRight":t.forward();break;case"ArrowLeft":t.rewind();break;case"f":t.fullscreen.toggle();break;case"c":u||t.toggleCaptions();break;case"l":t.loop=!t.loop}"Escape"===s&&!t.fullscreen.usingNative&&t.fullscreen.active&&t.fullscreen.toggle(),this.lastKey=s}else this.lastKey=null;var h}toggleMenu(e){xe.toggleMenu.call(this.player,e)}}function ze(e){return e&&e.__esModule&&Object.prototype.hasOwnProperty.call(e,"default")?e.default:e}var Ke,Ye={exports:{}};var Xe=(Ke||(Ke=1,function(e){e.exports=function(){var e=function(){},t={},i={},s={};function n(e,t){e=e.push?e:[e];var n,a,l,r=[],o=e.length,c=o;for(n=function(e,i){i.length&&r.push(e),--c||t(r)};o--;)a=e[o],(l=i[a])?n(a,l):(s[a]=s[a]||[]).push(n)}function a(e,t){if(e){var n=s[e];if(i[e]=t,n)for(;n.length;)n[0](e,t),n.splice(0,1)}}function l(t,i){t.call&&(t={success:t}),i.length?(t.error||e)(i):(t.success||e)(t)}function r(t,i,s,n){var a,l,o,c=document,u=s.async,h=(s.numRetries||0)+1,d=s.before||e,m=t.replace(/[\?|#].*$/,""),p=t.replace(/^(css|img|module|nomodule)!/,"");if(n=n||0,/(^css!|\.css$)/.test(m))(o=c.createElement("link")).rel="stylesheet",o.href=p,(a="hideFocus"in o)&&o.relList&&(a=0,o.rel="preload",o.as="style");else if(/(^img!|\.(png|gif|jpg|svg|webp)$)/.test(m))(o=c.createElement("img")).src=p;else if((o=c.createElement("script")).src=p,o.async=void 0===u||u,l="noModule"in o,/^module!/.test(m)){if(!l)return i(t,"l");o.type="module"}else if(/^nomodule!/.test(m)&&l)return i(t,"l");o.onload=o.onerror=o.onbeforeload=function(e){var l=e.type[0];if(a)try{o.sheet.cssText.length||(l="e")}catch(e){18!=e.code&&(l="e")}if("e"==l){if((n+=1)<h)return r(t,i,s,n)}else if("preload"==o.rel&&"style"==o.as)return o.rel="stylesheet";i(t,l,e.defaultPrevented)},!1!==d(t,o)&&c.head.appendChild(o)}function o(e,t,i){var s,n,a=(e=e.push?e:[e]).length,l=a,o=[];for(s=function(e,i,s){if("e"==i&&o.push(e),"b"==i){if(!s)return;o.push(e)}--a||t(o)},n=0;n<l;n++)r(e[n],s,i)}function c(e,i,s){var n,r;if(i&&i.trim&&(n=i),r=(n?s:i)||{},n){if(n in t)throw"LoadJS";t[n]=!0}function c(t,i){o(e,function(e){l(r,e),t&&l({success:t,error:i},e),a(n,e)},r)}if(r.returnPromise)return new Promise(c);c()}return c.ready=function(e,t){return n(e,function(e){l(t,e)}),c},c.done=function(e){a(e,[])},c.reset=function(){t={},i={},s={}},c.isDefined=function(e){return e in t},c}()}(Ye)),Ye.exports),Qe=ze(Xe);function Je(e){return new Promise((t,i)=>{Qe(e,{success:t,error:i})})}function Ge(e){e&&!this.embed.hasPlayed&&(this.embed.hasPlayed=!0),this.media.paused===e&&(this.media.paused=!e,se.call(this,this.media,e?"play":"pause"))}const Ze={setup(){const e=this;B(e.elements.wrapper,e.config.classNames.embed,!0),e.options.speed=e.config.speed.options,pe.call(e),x.object(window.Vimeo)?Ze.ready.call(e):Je(e.config.urls.vimeo.sdk).then(()=>{Ze.ready.call(e)}).catch(t=>{e.debug.warn("Vimeo SDK (player.js) failed to load",t)})},ready(){const e=this,t=e.config.vimeo,{premium:i,referrerPolicy:s,...n}=t;let a=e.media.getAttribute("src"),l="";x.empty(a)?(a=e.media.getAttribute(e.config.attributes.embed.id),l=e.media.getAttribute(e.config.attributes.embed.hash)):l=function(e){const t=e.match(/^.*(vimeo.com\/|video\/)(\d+)(\?.*h=|\/)+([\d,a-f]+)/);return t&&5===t.length?t[4]:null}(a);const r=l?{h:l}:{};i&&Object.assign(n,{controls:!1,sidedock:!1});const o=Ie({loop:e.config.loop.active,autoplay:e.autoplay,muted:e.muted,gesture:"media",playsinline:e.config.playsinline,...r,...n}),c=function(e){if(x.empty(e))return null;if(x.number(Number(e)))return e;const t=e.match(/^.*(vimeo.com\/|video\/)(\d+).*/);return t?t[2]:e}(a),u=q("iframe"),h=be(e.config.urls.vimeo.iframe,c,o);if(u.setAttribute("src",h),u.setAttribute("allowfullscreen",""),u.setAttribute("allow",["autoplay","fullscreen","picture-in-picture","encrypted-media","accelerometer","gyroscope"].join("; ")),x.empty(s)||u.setAttribute("referrerPolicy",s),i||!t.customControls)u.setAttribute("data-poster",e.poster),e.media=F(u,e.media);else{const t=q("div",{class:e.config.classNames.embedContainer,"data-poster":e.poster});t.appendChild(u),e.media=F(t,e.media)}t.customControls||Ee(be(e.config.urls.vimeo.api,h)).then(t=>{!x.empty(t)&&t.thumbnail_url&&Be.setPoster.call(e,t.thumbnail_url).catch(()=>{})}),e.embed=new window.Vimeo.Player(u,{autopause:e.config.autopause,muted:e.muted}),e.media.paused=!0,e.media.currentTime=0,e.supported.ui&&e.embed.disableTextTrack(),e.media.play=()=>(Ge.call(e,!0),e.embed.play()),e.media.pause=()=>(Ge.call(e,!1),e.embed.pause()),e.media.stop=()=>{e.pause(),e.currentTime=0};let{currentTime:d}=e.media;Object.defineProperty(e.media,"currentTime",{get:()=>d,set(t){const{embed:i,media:s,paused:n,volume:a}=e,l=n&&!i.hasPlayed;s.seeking=!0,se.call(e,s,"seeking"),Promise.resolve(l&&i.setVolume(0)).then(()=>i.setCurrentTime(t)).then(()=>l&&i.pause()).then(()=>l&&i.setVolume(a)).catch(()=>{})}});let m=e.config.speed.selected;Object.defineProperty(e.media,"playbackRate",{get:()=>m,set(t){e.embed.setPlaybackRate(t).then(()=>{m=t,se.call(e,e.media,"ratechange")}).catch(()=>{e.options.speed=[1]})}});let{volume:p}=e.config;Object.defineProperty(e.media,"volume",{get:()=>p,set(t){e.embed.setVolume(t).then(()=>{p=t,se.call(e,e.media,"volumechange")})}});let{muted:g}=e.config;Object.defineProperty(e.media,"muted",{get:()=>g,set(t){const i=!!x.boolean(t)&&t;e.embed.setMuted(!!i||e.config.muted).then(()=>{g=i,se.call(e,e.media,"volumechange")})}});let f,{loop:y}=e.config;Object.defineProperty(e.media,"loop",{get:()=>y,set(t){const i=x.boolean(t)?t:e.config.loop.active;e.embed.setLoop(i).then(()=>{y=i})}}),e.embed.getVideoUrl().then(t=>{f=t,xe.setDownloadUrl.call(e)}).catch(e=>{this.debug.warn(e)}),Object.defineProperty(e.media,"currentSrc",{get:()=>f}),Object.defineProperty(e.media,"ended",{get:()=>e.currentTime===e.duration}),Promise.all([e.embed.getVideoWidth(),e.embed.getVideoHeight()]).then(t=>{const[i,s]=t;e.embed.ratio=ge(i,s),pe.call(this)}),e.embed.setAutopause(e.config.autopause).then(t=>{e.config.autopause=t}),e.embed.getVideoTitle().then(t=>{e.config.title=t,Be.setTitle.call(this)}),e.embed.getCurrentTime().then(t=>{d=t,se.call(e,e.media,"timeupdate")}),e.embed.getDuration().then(t=>{e.media.duration=t,se.call(e,e.media,"durationchange")}),e.embed.getTextTracks().then(t=>{e.media.textTracks=t,$e.setup.call(e)}),e.embed.on("cuechange",({cues:t=[]})=>{const i=t.map(e=>function(e){const t=document.createDocumentFragment(),i=document.createElement("div");return t.appendChild(i),i.innerHTML=e,t.firstChild.textContent}(e.text));$e.updateCues.call(e,i)}),e.embed.on("loaded",()=>{if(e.embed.getPaused().then(t=>{Ge.call(e,!t),t||se.call(e,e.media,"playing")}),x.element(e.embed.element)&&e.supported.ui){e.embed.element.setAttribute("tabindex",-1)}}),e.embed.on("bufferstart",()=>{se.call(e,e.media,"waiting")}),e.embed.on("bufferend",()=>{se.call(e,e.media,"playing")}),e.embed.on("play",()=>{Ge.call(e,!0),se.call(e,e.media,"playing")}),e.embed.on("pause",()=>{Ge.call(e,!1)}),e.embed.on("timeupdate",t=>{e.media.seeking=!1,d=t.seconds,se.call(e,e.media,"timeupdate")}),e.embed.on("progress",t=>{e.media.buffered=t.percent,se.call(e,e.media,"progress"),1===Number.parseInt(t.percent,10)&&se.call(e,e.media,"canplaythrough"),e.embed.getDuration().then(t=>{t!==e.media.duration&&(e.media.duration=t,se.call(e,e.media,"durationchange"))})}),e.embed.on("seeked",()=>{e.media.seeking=!1,se.call(e,e.media,"seeked")}),e.embed.on("ended",()=>{e.media.paused=!0,se.call(e,e.media,"ended")}),e.embed.on("error",t=>{e.media.error=t,se.call(e,e.media,"error")}),t.customControls&&setTimeout(()=>Be.build.call(e),0)}};function et(e){e&&!this.embed.hasPlayed&&(this.embed.hasPlayed=!0),this.media.paused===e&&(this.media.paused=!e,se.call(this,this.media,e?"play":"pause"))}function tt(e){return e.noCookie?"https://www.youtube-nocookie.com":"http:"===window.location.protocol?"http://www.youtube.com":void 0}const it={setup(){if(B(this.elements.wrapper,this.config.classNames.embed,!0),x.object(window.YT)&&x.function(window.YT.Player))it.ready.call(this);else{const e=window.onYouTubeIframeAPIReady;window.onYouTubeIframeAPIReady=()=>{x.function(e)&&e(),it.ready.call(this)},Je(this.config.urls.youtube.sdk).catch(e=>{this.debug.warn("YouTube API failed to load",e)})}},getTitle(e){Ee(be(this.config.urls.youtube.api,e)).then(e=>{if(x.object(e)){const{title:t,height:i,width:s}=e;this.config.title=t,Be.setTitle.call(this),this.embed.ratio=ge(s,i)}pe.call(this)}).catch(()=>{pe.call(this)})},ready(){const e=this,t=e.config.youtube,i=e.media&&e.media.getAttribute("id");if(!x.empty(i)&&i.startsWith("youtube-"))return;let s=e.media.getAttribute("src");x.empty(s)&&(s=e.media.getAttribute(this.config.attributes.embed.id));const n=function(e){if(x.empty(e))return null;const t=e.match(/^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/);return t&&t[2]?t[2]:e}(s);const a=q("div",{id:`${e.provider}-${Math.floor(1e4*Math.random())}`,"data-poster":t.customControls?e.poster:void 0});if(e.media=F(a,e.media),t.customControls){const t=e=>`https://i.ytimg.com/vi/${n}/${e}default.jpg`;Ue(t("maxres"),121).catch(()=>Ue(t("sd"),121)).catch(()=>Ue(t("hq"))).then(t=>Be.setPoster.call(e,t.src)).then(t=>{t.includes("maxres")||(e.elements.poster.style.backgroundSize="cover")}).catch(()=>{})}e.embed=new window.YT.Player(e.media,{videoId:n,host:tt(t),playerVars:_({},{autoplay:e.config.autoplay?1:0,hl:e.config.hl,controls:e.supported.ui&&t.customControls?0:1,disablekb:1,playsinline:e.config.playsinline&&!e.config.fullscreen.iosNative?1:0,cc_load_policy:e.captions.active?1:0,cc_lang_pref:e.config.captions.language,widget_referrer:window?window.location.href:null},t),events:{onError(t){if(!e.media.error){const i=t.data,s={2:"The request contains an invalid parameter value. For example, this error occurs if you specify a video ID that does not have 11 characters, or if the video ID contains invalid characters, such as exclamation points or asterisks.",5:"The requested content cannot be played in an HTML5 player or another error related to the HTML5 player has occurred.",100:"The video requested was not found. This error occurs when a video has been removed (for any reason) or has been marked as private.",101:"The owner of the requested video does not allow it to be played in embedded players.",150:"The owner of the requested video does not allow it to be played in embedded players."}[i]||"An unknown error occurred";e.media.error={code:i,message:s},se.call(e,e.media,"error")}},onPlaybackRateChange(t){const i=t.target;e.media.playbackRate=i.getPlaybackRate(),se.call(e,e.media,"ratechange")},onReady(i){if(x.function(e.media.play))return;const s=i.target;it.getTitle.call(e,n),e.media.play=()=>{et.call(e,!0),s.playVideo()},e.media.pause=()=>{et.call(e,!1),s.pauseVideo()},e.media.stop=()=>{s.stopVideo()},e.media.duration=s.getDuration(),e.media.paused=!0,e.media.currentTime=0,Object.defineProperty(e.media,"currentTime",{get:()=>Number(s.getCurrentTime()),set(t){e.paused&&!e.embed.hasPlayed&&e.embed.mute(),e.media.seeking=!0,se.call(e,e.media,"seeking"),s.seekTo(t)}}),Object.defineProperty(e.media,"playbackRate",{get:()=>s.getPlaybackRate(),set(e){s.setPlaybackRate(e)}});let{volume:a}=e.config;Object.defineProperty(e.media,"volume",{get:()=>a,set(t){a=t,s.setVolume(100*a),se.call(e,e.media,"volumechange")}});let{muted:l}=e.config;Object.defineProperty(e.media,"muted",{get:()=>l,set(t){const i=x.boolean(t)?t:l;l=i,s[i?"mute":"unMute"](),s.setVolume(100*a),se.call(e,e.media,"volumechange")}}),Object.defineProperty(e.media,"currentSrc",{get:()=>s.getVideoUrl()}),Object.defineProperty(e.media,"ended",{get:()=>e.currentTime===e.duration});const r=s.getAvailablePlaybackRates();e.options.speed=r.filter(t=>e.config.speed.options.includes(t)),e.supported.ui&&t.customControls&&e.media.setAttribute("tabindex",-1),se.call(e,e.media,"timeupdate"),se.call(e,e.media,"durationchange"),clearInterval(e.timers.buffering),e.timers.buffering=setInterval(()=>{e.media.buffered=s.getVideoLoadedFraction(),(null===e.media.lastBuffered||e.media.lastBuffered<e.media.buffered)&&se.call(e,e.media,"progress"),e.media.lastBuffered=e.media.buffered,1===e.media.buffered&&(clearInterval(e.timers.buffering),se.call(e,e.media,"canplaythrough"))},200),t.customControls&&setTimeout(()=>Be.build.call(e),50)},onStateChange(i){const s=i.target;clearInterval(e.timers.playing);switch(e.media.seeking&&[1,2].includes(i.data)&&(e.media.seeking=!1,se.call(e,e.media,"seeked")),i.data){case-1:se.call(e,e.media,"timeupdate"),e.media.buffered=s.getVideoLoadedFraction(),se.call(e,e.media,"progress");break;case 0:et.call(e,!1),e.media.loop?(s.stopVideo(),s.playVideo()):se.call(e,e.media,"ended");break;case 1:t.customControls&&!e.config.autoplay&&e.media.paused&&!e.embed.hasPlayed?e.media.pause():(et.call(e,!0),se.call(e,e.media,"playing"),e.timers.playing=setInterval(()=>{se.call(e,e.media,"timeupdate")},50),e.media.duration!==s.getDuration()&&(e.media.duration=s.getDuration(),se.call(e,e.media,"durationchange")));break;case 2:e.muted||e.embed.unMute(),et.call(e,!1);break;case 3:se.call(e,e.media,"waiting")}se.call(e,e.elements.container,"statechange",!1,{code:i.data})}}})}},st={setup(){this.media?(B(this.elements.container,this.config.classNames.type.replace("{0}",this.type),!0),B(this.elements.container,this.config.classNames.provider.replace("{0}",this.provider),!0),this.isEmbed&&B(this.elements.container,this.config.classNames.type.replace("{0}","video"),!0),this.isVideo&&(this.elements.wrapper=q("div",{class:this.config.classNames.video}),O(this.media,this.elements.wrapper),this.elements.poster=q("div",{class:this.config.classNames.poster}),this.elements.wrapper.appendChild(this.elements.poster)),this.isHTML5?fe.setup.call(this):this.isYouTube?it.setup.call(this):this.isVimeo&&Ze.setup.call(this)):this.debug.warn("No media element found!")}};class nt{constructor(t){e(this,"load",()=>{this.enabled&&(x.object(window.google)&&x.object(window.google.ima)?this.ready():Je(this.player.config.urls.googleIMA.sdk).then(()=>{this.ready()}).catch(()=>{this.trigger("error",new Error("Google IMA SDK failed to load"))}))}),e(this,"ready",()=>{var e;this.enabled||((e=this).manager&&e.manager.destroy(),e.elements.displayContainer&&e.elements.displayContainer.destroy(),e.elements.container.remove()),this.startSafetyTimer(12e3,"ready()"),this.managerPromise.then(()=>{this.clearSafetyTimer("onAdsManagerLoaded()")}),this.listeners(),this.setupIMA()}),e(this,"setupIMA",()=>{this.elements.container=q("div",{class:this.player.config.classNames.ads}),this.player.elements.container.appendChild(this.elements.container),google.ima.settings.setVpaidMode(google.ima.ImaSdkSettings.VpaidMode.ENABLED),google.ima.settings.setLocale(this.player.config.ads.language),google.ima.settings.setDisableCustomPlaybackForIOS10Plus(this.player.config.playsinline),this.elements.displayContainer=new google.ima.AdDisplayContainer(this.elements.container,this.player.media),this.loader=new google.ima.AdsLoader(this.elements.displayContainer),this.loader.addEventListener(google.ima.AdsManagerLoadedEvent.Type.ADS_MANAGER_LOADED,e=>this.onAdsManagerLoaded(e),!1),this.loader.addEventListener(google.ima.AdErrorEvent.Type.AD_ERROR,e=>this.onAdError(e),!1),this.requestAds()}),e(this,"requestAds",()=>{const{container:e}=this.player.elements;try{const t=new google.ima.AdsRequest;t.adTagUrl=this.tagUrl,t.linearAdSlotWidth=e.offsetWidth,t.linearAdSlotHeight=e.offsetHeight,t.nonLinearAdSlotWidth=e.offsetWidth,t.nonLinearAdSlotHeight=e.offsetHeight,t.forceNonLinearFullSlot=!1,t.setAdWillPlayMuted(!this.player.muted),this.loader.requestAds(t)}catch(e){this.onAdError(e)}}),e(this,"pollCountdown",(e=!1)=>{if(!e)return clearInterval(this.countdownTimer),void this.elements.container.removeAttribute("data-badge-text");this.countdownTimer=setInterval(()=>{const e=Ne(Math.max(this.manager.getRemainingTime(),0)),t=`${Ae.get("advertisement",this.player.config)} - ${e}`;this.elements.container.setAttribute("data-badge-text",t)},100)}),e(this,"onAdsManagerLoaded",e=>{if(!this.enabled)return;const t=new google.ima.AdsRenderingSettings;t.restoreCustomPlaybackStateOnAdBreakComplete=!0,t.enablePreloading=!0,this.manager=e.getAdsManager(this.player,t),this.cuePoints=this.manager.getCuePoints(),this.manager.addEventListener(google.ima.AdErrorEvent.Type.AD_ERROR,e=>this.onAdError(e)),Object.keys(google.ima.AdEvent.Type).forEach(e=>{this.manager.addEventListener(google.ima.AdEvent.Type[e],e=>this.onAdEvent(e))}),this.trigger("loaded")}),e(this,"addCuePoints",()=>{x.empty(this.cuePoints)||this.cuePoints.forEach(e=>{if(0!==e&&-1!==e&&e<this.player.duration){const t=this.player.elements.progress;if(x.element(t)){const i=100/this.player.duration*e,s=q("span",{class:this.player.config.classNames.cues});s.style.left=`${i.toString()}%`,t.appendChild(s)}}})}),e(this,"onAdEvent",e=>{const{container:t}=this.player.elements,i=e.getAd(),s=e.getAdData();switch((e=>{se.call(this.player,this.player.media,`ads${e.replace(/_/g,"").toLowerCase()}`)})(e.type),e.type){case google.ima.AdEvent.Type.LOADED:this.trigger("loaded"),this.pollCountdown(!0),i.isLinear()||(i.width=t.offsetWidth,i.height=t.offsetHeight);break;case google.ima.AdEvent.Type.STARTED:this.manager.setVolume(this.player.volume);break;case google.ima.AdEvent.Type.ALL_ADS_COMPLETED:this.player.ended?this.loadAds():this.loader.contentComplete();break;case google.ima.AdEvent.Type.CONTENT_PAUSE_REQUESTED:this.pauseContent();break;case google.ima.AdEvent.Type.CONTENT_RESUME_REQUESTED:this.pollCountdown(),this.resumeContent();break;case google.ima.AdEvent.Type.LOG:s.adError&&this.player.debug.warn(`Non-fatal ad error: ${s.adError.getMessage()}`)}}),e(this,"onAdError",e=>{this.cancel(),this.player.debug.warn("Ads error",e)}),e(this,"listeners",()=>{const{container:e}=this.player.elements;let t;this.player.on("canplay",()=>{this.addCuePoints()}),this.player.on("ended",()=>{this.loader.contentComplete()}),this.player.on("timeupdate",()=>{t=this.player.currentTime}),this.player.on("seeked",()=>{const e=this.player.currentTime;x.empty(this.cuePoints)||this.cuePoints.forEach((i,s)=>{t<i&&i<e&&(this.manager.discardAdBreak(),this.cuePoints.splice(s,1))})}),window.addEventListener("resize",()=>{this.manager&&this.manager.resize(e.offsetWidth,e.offsetHeight,google.ima.ViewMode.NORMAL)})}),e(this,"play",()=>{const{container:e}=this.player.elements;this.managerPromise||this.resumeContent(),this.managerPromise.then(()=>{this.manager.setVolume(this.player.volume),this.elements.displayContainer.initialize();try{this.initialized||(this.manager.init(e.offsetWidth,e.offsetHeight,google.ima.ViewMode.NORMAL),this.manager.start()),this.initialized=!0}catch(e){this.onAdError(e)}}).catch(()=>{})}),e(this,"resumeContent",()=>{this.elements.container.style.zIndex="",this.playing=!1,le(this.player.media.play())}),e(this,"pauseContent",()=>{this.elements.container.style.zIndex=3,this.playing=!0,this.player.media.pause()}),e(this,"cancel",()=>{this.initialized&&this.resumeContent(),this.trigger("error"),this.loadAds()}),e(this,"loadAds",()=>{this.managerPromise.then(()=>{this.manager&&this.manager.destroy(),this.managerPromise=new Promise(e=>{this.on("loaded",e),this.player.debug.log(this.manager)}),this.initialized=!1,this.requestAds()}).catch(()=>{})}),e(this,"trigger",(e,...t)=>{const i=this.events[e];x.array(i)&&i.forEach(e=>{x.function(e)&&e.apply(this,t)})}),e(this,"on",(e,t)=>(x.array(this.events[e])||(this.events[e]=[]),this.events[e].push(t),this)),e(this,"startSafetyTimer",(e,t)=>{this.player.debug.log(`Safety timer invoked from: ${t}`),this.safetyTimer=setTimeout(()=>{this.cancel(),this.clearSafetyTimer("startSafetyTimer()")},e)}),e(this,"clearSafetyTimer",e=>{x.nullOrUndefined(this.safetyTimer)||(this.player.debug.log(`Safety timer cleared from: ${e}`),clearTimeout(this.safetyTimer),this.safetyTimer=null)}),this.player=t,this.config=t.config.ads,this.playing=!1,this.initialized=!1,this.elements={container:null,displayContainer:null},this.manager=null,this.loader=null,this.cuePoints=null,this.events={},this.safetyTimer=null,this.countdownTimer=null,this.managerPromise=new Promise((e,t)=>{this.on("loaded",e),this.on("error",t)}),this.load()}get enabled(){const{config:e}=this;return this.player.isHTML5&&this.player.isVideo&&e.enabled&&(!x.empty(e.publisherId)||x.url(e.tagUrl))}get tagUrl(){const{config:e}=this;if(x.url(e.tagUrl))return e.tagUrl;return`https://go.aniview.com/api/adserver6/vast/?${Ie({AV_PUBLISHERID:"58c25bb0073ef448b1087ad6",AV_CHANNELID:"5a0458dc28a06145e4519d21",AV_URL:window.location.hostname,cb:Date.now(),AV_WIDTH:640,AV_HEIGHT:480,AV_CDIM2:e.publisherId})}`}}function at(e=0,t=0,i=255){return Math.min(Math.max(e,t),i)}function lt(e){const t=[];return e.split(/\r\n\r\n|\n\n|\r\r/).forEach(e=>{const i={};e.split(/\r\n|\n|\r/).forEach(e=>{if(x.number(i.startTime)){if(!x.empty(e.trim())&&x.empty(i.text)){const t=e.trim().split("#xywh=");[i.text]=t,t[1]&&([i.x,i.y,i.w,i.h]=t[1].split(","))}}else{const t=e.match(/(\d{2})?:?(\d{2}):(\d{2}).(\d{2,3})( ?--> ?)(\d{2})?:?(\d{2}):(\d{2}).(\d{2,3})/);t&&(i.startTime=60*Number(t[1]||0)*60+60*Number(t[2])+Number(t[3])+Number(`0.${t[4]}`),i.endTime=60*Number(t[6]||0)*60+60*Number(t[7])+Number(t[8])+Number(`0.${t[9]}`))}}),i.text&&t.push(i)}),t}function rt(e,t){const i={};return e>t.width/t.height?(i.width=t.width,i.height=1/e*t.width):(i.height=t.height,i.width=e*t.height),i}class ot{constructor(t){e(this,"load",()=>{this.player.elements.display.seekTooltip&&(this.player.elements.display.seekTooltip.hidden=this.enabled),this.enabled&&this.getThumbnails().then(()=>{this.enabled&&(this.render(),this.determineContainerAutoSizing(),this.listeners(),this.loaded=!0)})}),e(this,"getThumbnails",()=>new Promise(e=>{const{src:t}=this.player.config.previewThumbnails;if(x.empty(t))throw new Error("Missing previewThumbnails.src config attribute");const i=()=>{this.thumbnails.sort((e,t)=>e.height-t.height),this.player.debug.log("Preview thumbnails",this.thumbnails),e()};if(x.function(t))t(e=>{this.thumbnails=e,i()});else{const e=(x.string(t)?[t]:t).map(e=>this.getThumbnail(e));Promise.all(e).then(i)}})),e(this,"getThumbnail",e=>new Promise(t=>{Ee(e,void 0,this.player.config.previewThumbnails.withCredentials).then(i=>{const s={frames:lt(i),height:null,urlPrefix:""};s.frames[0].text.startsWith("/")||s.frames[0].text.startsWith("http://")||s.frames[0].text.startsWith("https://")||(s.urlPrefix=e.substring(0,e.lastIndexOf("/")+1));const n=new Image;n.onload=()=>{s.height=n.naturalHeight,s.width=n.naturalWidth,this.thumbnails.push(s),t()},n.src=s.urlPrefix+s.frames[0].text})})),e(this,"startMove",e=>{if(this.loaded&&x.event(e)&&["touchmove","mousemove"].includes(e.type)&&this.player.media.duration){if("touchmove"===e.type)this.seekTime=this.player.media.duration*(this.player.elements.inputs.seek.value/100);else{var t,i;const s=this.player.elements.progress.getBoundingClientRect(),n=100/s.width*(e.pageX-s.left);this.seekTime=this.player.media.duration*(n/100),this.seekTime<0&&(this.seekTime=0),this.seekTime>this.player.media.duration-1&&(this.seekTime=this.player.media.duration-1),this.mousePosX=e.pageX,this.elements.thumb.time.textContent=Ne(this.seekTime);const a=null===(t=this.player.config.markers)||void 0===t||null===(i=t.points)||void 0===i?void 0:i.find(({time:e})=>e===Math.round(this.seekTime));a&&this.elements.thumb.time.insertAdjacentHTML("afterbegin",`${a.label}<br>`)}this.showImageAtCurrentTime()}}),e(this,"endMove",()=>{this.toggleThumbContainer(!1,!0)}),e(this,"startScrubbing",e=>{(x.nullOrUndefined(e.button)||!1===e.button||0===e.button)&&(this.mouseDown=!0,this.player.media.duration&&(this.toggleScrubbingContainer(!0),this.toggleThumbContainer(!1,!0),this.showImageAtCurrentTime()))}),e(this,"endScrubbing",()=>{this.mouseDown=!1,Math.ceil(this.lastTime)===Math.ceil(this.player.media.currentTime)?this.toggleScrubbingContainer(!1):ie.call(this.player,this.player.media,"timeupdate",()=>{this.mouseDown||this.toggleScrubbingContainer(!1)})}),e(this,"listeners",()=>{this.player.on("play",()=>{this.toggleThumbContainer(!1,!0)}),this.player.on("seeked",()=>{this.toggleThumbContainer(!1)}),this.player.on("timeupdate",()=>{this.lastTime=this.player.media.currentTime})}),e(this,"render",()=>{this.elements.thumb.container=q("div",{class:this.player.config.classNames.previewThumbnails.thumbContainer}),this.elements.thumb.imageContainer=q("div",{class:this.player.config.classNames.previewThumbnails.imageContainer}),this.elements.thumb.container.appendChild(this.elements.thumb.imageContainer);const e=q("div",{class:this.player.config.classNames.previewThumbnails.timeContainer});this.elements.thumb.time=q("span",{},"00:00"),e.appendChild(this.elements.thumb.time),this.elements.thumb.imageContainer.appendChild(e),x.element(this.player.elements.progress)&&this.player.elements.progress.appendChild(this.elements.thumb.container),this.elements.scrubbing.container=q("div",{class:this.player.config.classNames.previewThumbnails.scrubbingContainer}),this.player.elements.wrapper.appendChild(this.elements.scrubbing.container)}),e(this,"destroy",()=>{this.elements.thumb.container&&this.elements.thumb.container.remove(),this.elements.scrubbing.container&&this.elements.scrubbing.container.remove()}),e(this,"showImageAtCurrentTime",()=>{this.mouseDown?this.setScrubbingContainerSize():this.setThumbContainerSizeAndPos();const e=this.thumbnails[0].frames.findIndex(e=>this.seekTime>=e.startTime&&this.seekTime<=e.endTime),t=e>=0;let i=0;this.mouseDown||this.toggleThumbContainer(t),t&&(this.thumbnails.forEach((t,s)=>{this.loadedImages.includes(t.frames[e].text)&&(i=s)}),e!==this.showingThumb&&(this.showingThumb=e,this.loadImage(i)))}),e(this,"loadImage",(e=0)=>{const t=this.showingThumb,i=this.thumbnails[e],{urlPrefix:s}=i,n=i.frames[t],a=i.frames[t].text,l=s+a;if(this.currentImageElement&&this.currentImageElement.dataset.filename===a)this.showImage(this.currentImageElement,n,e,t,a,!1),this.currentImageElement.dataset.index=t,this.removeOldImages(this.currentImageElement);else{this.loadingImage&&this.usingSprites&&(this.loadingImage.onload=null);const i=new Image;i.src=l,i.dataset.index=t,i.dataset.filename=a,this.showingThumbFilename=a,this.player.debug.log(`Loading image: ${l}`),i.onload=()=>this.showImage(i,n,e,t,a,!0),this.loadingImage=i,this.removeOldImages(i)}}),e(this,"showImage",(e,t,i,s,n,a=!0)=>{this.player.debug.log(`Showing thumb: ${n}. num: ${s}. qual: ${i}. newimg: ${a}`),this.setImageSizeAndOffset(e,t),a&&(this.currentImageContainer.appendChild(e),this.currentImageElement=e,this.loadedImages.includes(n)||this.loadedImages.push(n)),this.preloadNearby(s,!0).then(this.preloadNearby(s,!1)).then(this.getHigherQuality(i,e,t,n))}),e(this,"removeOldImages",e=>{Array.from(this.currentImageContainer.children).forEach(t=>{if("img"!==t.tagName.toLowerCase())return;const i=this.usingSprites?500:1e3;if(t.dataset.index!==e.dataset.index&&!t.dataset.deleting){t.dataset.deleting=!0;const{currentImageContainer:e}=this;setTimeout(()=>{e.removeChild(t),this.player.debug.log(`Removing thumb: ${t.dataset.filename}`)},i)}})}),e(this,"preloadNearby",(e,t=!0)=>new Promise(i=>{setTimeout(()=>{const s=this.thumbnails[0].frames[e].text;if(this.showingThumbFilename===s){let n;n=t?this.thumbnails[0].frames.slice(e):this.thumbnails[0].frames.slice(0,e).reverse();let a=!1;n.forEach(e=>{const t=e.text;if(t!==s&&!this.loadedImages.includes(t)){a=!0,this.player.debug.log(`Preloading thumb filename: ${t}`);const{urlPrefix:e}=this.thumbnails[0],s=e+t,n=new Image;n.src=s,n.onload=()=>{this.player.debug.log(`Preloaded thumb filename: ${t}`),this.loadedImages.includes(t)||this.loadedImages.push(t),i()}}}),a||i()}},300)})),e(this,"getHigherQuality",(e,t,i,s)=>{if(e<this.thumbnails.length-1){let n=t.naturalHeight;this.usingSprites&&(n=i.h),n<this.thumbContainerHeight&&setTimeout(()=>{this.showingThumbFilename===s&&(this.player.debug.log(`Showing higher quality thumb for: ${s}`),this.loadImage(e+1))},300)}}),e(this,"toggleThumbContainer",(e=!1,t=!1)=>{const i=this.player.config.classNames.previewThumbnails.thumbContainerShown;this.elements.thumb.container.classList.toggle(i,e),!e&&t&&(this.showingThumb=null,this.showingThumbFilename=null)}),e(this,"toggleScrubbingContainer",(e=!1)=>{const t=this.player.config.classNames.previewThumbnails.scrubbingContainerShown;this.elements.scrubbing.container.classList.toggle(t,e),e||(this.showingThumb=null,this.showingThumbFilename=null)}),e(this,"determineContainerAutoSizing",()=>{(this.elements.thumb.imageContainer.clientHeight>20||this.elements.thumb.imageContainer.clientWidth>20)&&(this.sizeSpecifiedInCSS=!0)}),e(this,"setThumbContainerSizeAndPos",()=>{const{imageContainer:e}=this.elements.thumb;if(this.sizeSpecifiedInCSS){if(e.clientHeight>20&&e.clientWidth<20){const t=Math.floor(e.clientHeight*this.thumbAspectRatio);e.style.width=`${t}px`}else if(e.clientHeight<20&&e.clientWidth>20){const t=Math.floor(e.clientWidth/this.thumbAspectRatio);e.style.height=`${t}px`}}else{const t=Math.floor(this.thumbContainerHeight*this.thumbAspectRatio);e.style.height=`${this.thumbContainerHeight}px`,e.style.width=`${t}px`}this.setThumbContainerPos()}),e(this,"setThumbContainerPos",()=>{const e=this.player.elements.progress.getBoundingClientRect(),t=this.player.elements.container.getBoundingClientRect(),{container:i}=this.elements.thumb,s=t.left-e.left+10,n=t.right-e.left-i.clientWidth-10,a=this.mousePosX-e.left-i.clientWidth/2,l=at(a,s,n);i.style.left=`${l}px`,i.style.setProperty("--preview-arrow-offset",a-l+"px")}),e(this,"setScrubbingContainerSize",()=>{const{width:e,height:t}=rt(this.thumbAspectRatio,{width:this.player.media.clientWidth,height:this.player.media.clientHeight});this.elements.scrubbing.container.style.width=`${e}px`,this.elements.scrubbing.container.style.height=`${t}px`}),e(this,"setImageSizeAndOffset",(e,t)=>{if(!this.usingSprites)return;const i=this.thumbContainerHeight/t.h;e.style.height=e.naturalHeight*i+"px",e.style.width=e.naturalWidth*i+"px",e.style.left=`-${t.x*i}px`,e.style.top=`-${t.y*i}px`}),this.player=t,this.thumbnails=[],this.loaded=!1,this.lastMouseMoveTime=Date.now(),this.mouseDown=!1,this.loadedImages=[],this.elements={thumb:{},scrubbing:{}},this.load()}get enabled(){return this.player.isHTML5&&this.player.isVideo&&this.player.config.previewThumbnails.enabled}get currentImageContainer(){return this.mouseDown?this.elements.scrubbing.container:this.elements.thumb.imageContainer}get usingSprites(){return Object.keys(this.thumbnails[0].frames[0]).includes("w")}get thumbAspectRatio(){return this.usingSprites?this.thumbnails[0].frames[0].w/this.thumbnails[0].frames[0].h:this.thumbnails[0].width/this.thumbnails[0].height}get thumbContainerHeight(){if(this.mouseDown){const{height:e}=rt(this.thumbAspectRatio,{width:this.player.media.clientWidth,height:this.player.media.clientHeight});return e}return this.sizeSpecifiedInCSS?this.elements.thumb.imageContainer.clientHeight:Math.floor(this.player.media.clientWidth/this.thumbAspectRatio/4)}get currentImageElement(){return this.mouseDown?this.currentScrubbingImageElement:this.currentThumbnailImageElement}set currentImageElement(e){this.mouseDown?this.currentScrubbingImageElement=e:this.currentThumbnailImageElement=e}}const ct={insertElements(e,t){x.string(t)?D(e,this.media,{src:t}):x.array(t)&&t.forEach(t=>{D(e,this.media,t)})},change(e){$(e,"sources.length")?(fe.cancelRequests.call(this),this.destroy(()=>{this.options.quality=[],H(this.media),this.media=null,x.element(this.elements.container)&&this.elements.container.removeAttribute("class");const{sources:t,type:i}=e,[{provider:s=qe.html5,src:n}]=t,a="html5"===s?i:"div",l="html5"===s?{}:{src:n};Object.assign(this,{provider:s,type:i,supported:J.check(i,s,this.config.playsinline),media:q(a,l)}),this.elements.container.appendChild(this.media),x.boolean(e.autoplay)&&(this.config.autoplay=e.autoplay),this.isHTML5&&(this.config.crossorigin&&this.media.setAttribute("crossorigin",""),this.config.autoplay&&this.media.setAttribute("autoplay",""),x.empty(e.poster)||(this.poster=e.poster),this.config.loop.active&&this.media.setAttribute("loop",""),this.config.muted&&this.media.setAttribute("muted",""),this.config.playsinline&&this.media.setAttribute("playsinline","")),Be.addStyleHook.call(this),this.isHTML5&&ct.insertElements.call(this,"source",t),this.config.title=e.title,st.setup.call(this),this.isHTML5&&Object.keys(e).includes("tracks")&&ct.insertElements.call(this,"track",e.tracks),(this.isHTML5||this.isEmbed&&!this.supported.ui)&&Be.build.call(this),this.isHTML5&&this.media.load(),x.empty(e.previewThumbnails)||(Object.assign(this.config.previewThumbnails,e.previewThumbnails),this.previewThumbnails&&this.previewThumbnails.loaded&&(this.previewThumbnails.destroy(),this.previewThumbnails=null),this.config.previewThumbnails.enabled&&(this.previewThumbnails=new ot(this))),this.fullscreen.update()},!0)):this.debug.warn("Invalid source format")}};class ut{constructor(t,i){if(e(this,"play",()=>x.function(this.media.play)?(this.ads&&this.ads.enabled&&this.ads.managerPromise.then(()=>this.ads.play()).catch(()=>le(this.media.play())),this.media.play()):null),e(this,"pause",()=>this.playing&&x.function(this.media.pause)?this.media.pause():null),e(this,"togglePlay",e=>(x.boolean(e)?e:!this.playing)?this.play():this.pause()),e(this,"stop",()=>{this.isHTML5?(this.pause(),this.restart()):x.function(this.media.stop)&&this.media.stop()}),e(this,"restart",()=>{this.currentTime=0}),e(this,"rewind",e=>{this.currentTime-=x.number(e)?e:this.config.seekTime}),e(this,"forward",e=>{this.currentTime+=x.number(e)?e:this.config.seekTime}),e(this,"increaseVolume",e=>{const t=this.media.muted?0:this.volume;this.volume=t+(x.number(e)?e:0)}),e(this,"decreaseVolume",e=>{this.increaseVolume(-e)}),e(this,"airplay",()=>{J.airplay&&this.media.webkitShowPlaybackTargetPicker()}),e(this,"toggleControls",e=>{if(this.supported.ui&&!this.isAudio){const t=W(this.elements.container,this.config.classNames.hideControls),i=void 0===e?void 0:!e,s=B(this.elements.container,this.config.classNames.hideControls,i);if(s&&x.array(this.config.controls)&&this.config.controls.includes("settings")&&!x.empty(this.config.settings)&&xe.toggleMenu.call(this,!1),s!==t){const e=s?"controlshidden":"controlsshown";se.call(this,this.media,e)}return!s}return!1}),e(this,"on",(e,t)=>{ee.call(this,this.elements.container,e,t)}),e(this,"once",(e,t)=>{ie.call(this,this.elements.container,e,t)}),e(this,"off",(e,t)=>{te(this.elements.container,e,t)}),e(this,"destroy",(e,t=!1)=>{if(!this.ready)return;const i=()=>{document.body.style.overflow="",this.embed=null,t?(Object.keys(this.elements).length&&(H(this.elements.buttons.play),H(this.elements.captions),H(this.elements.controls),H(this.elements.wrapper),this.elements.buttons.play=null,this.elements.captions=null,this.elements.controls=null,this.elements.wrapper=null),x.function(e)&&e()):(ne.call(this),fe.cancelRequests.call(this),F(this.elements.original,this.elements.container),se.call(this,this.elements.original,"destroyed",!0),x.function(e)&&e.call(this.elements.original),this.ready=!1,setTimeout(()=>{this.elements=null,this.media=null},200))};this.stop(),clearTimeout(this.timers.loading),clearTimeout(this.timers.controls),clearTimeout(this.timers.resized),this.isHTML5?(Be.toggleNativeControls.call(this,!0),i()):this.isYouTube?(clearInterval(this.timers.buffering),clearInterval(this.timers.playing),null!==this.embed&&x.function(this.embed.destroy)&&this.embed.destroy(),i()):this.isVimeo&&(null!==this.embed&&this.embed.unload().then(i),setTimeout(i,200))}),e(this,"supports",e=>J.mime.call(this,e)),this.timers={},this.ready=!1,this.loading=!1,this.failed=!1,this.touch=J.touch,this.media=t,x.string(this.media)&&(this.media=document.querySelectorAll(this.media)),(window.jQuery&&this.media instanceof jQuery||x.nodeList(this.media)||x.array(this.media))&&(this.media=this.media[0]),this.config=_({},_e,ut.defaults,i||{},(()=>{try{return JSON.parse(this.media.getAttribute("data-plyr-config"))}catch{return{}}})()),this.elements={container:null,fullscreen:null,captions:null,buttons:{},display:{},progress:{},inputs:{},settings:{popup:null,menu:null,panels:{},buttons:{}}},this.captions={active:null,currentTrack:-1,meta:new WeakMap},this.fullscreen={active:!1},this.options={speed:[],quality:[]},this.debug=new Fe(this.config.debug),this.debug.log("Config",this.config),this.debug.log("Support",J),x.nullOrUndefined(this.media)||!x.element(this.media))return void this.debug.error("Setup failed: no suitable element passed");if(this.media.plyr)return void this.debug.warn("Target already setup");if(!this.config.enabled)return void this.debug.error("Setup failed: disabled by config");if(!J.check().api)return void this.debug.error("Setup failed: no support");const s=this.media.cloneNode(!0);s.autoplay=!1,this.elements.original=s;const n=this.media.tagName.toLowerCase();let a=null,l=null;switch(n){case"div":if(a=this.media.querySelector("iframe"),x.element(a)){if(l=Le(a.getAttribute("src")),this.provider=function(e){return/^(?:https?:\/\/)?(?:www\.)?(?:youtube\.com|youtube-nocookie\.com|youtu\.?be)\/.+$/.test(e)?qe.youtube:/^https?:\/\/player.vimeo.com\/video\/\d{0,9}(?=\b|\/)/.test(e)?qe.vimeo:null}(l.toString()),this.elements.container=this.media,this.media=a,this.elements.container.className="",l.search.length){const e=["1","true"];e.includes(l.searchParams.get("autoplay"))&&(this.config.autoplay=!0),e.includes(l.searchParams.get("loop"))&&(this.config.loop.active=!0),this.isYouTube?(this.config.playsinline=e.includes(l.searchParams.get("playsinline")),this.config.youtube.hl=l.searchParams.get("hl")):this.config.playsinline=!0}}else this.provider=this.media.getAttribute(this.config.attributes.embed.provider),this.media.removeAttribute(this.config.attributes.embed.provider);if(x.empty(this.provider)||!Object.values(qe).includes(this.provider))return void this.debug.error("Setup failed: Invalid provider");this.type=He;break;case"video":case"audio":this.type=n,this.provider=qe.html5,this.media.hasAttribute("crossorigin")&&(this.config.crossorigin=!0),this.media.hasAttribute("autoplay")&&(this.config.autoplay=!0),(this.media.hasAttribute("playsinline")||this.media.hasAttribute("webkit-playsinline"))&&(this.config.playsinline=!0),this.media.hasAttribute("muted")&&(this.config.muted=!0),this.media.hasAttribute("loop")&&(this.config.loop.active=!0);break;default:return void this.debug.error("Setup failed: unsupported type")}this.supported=J.check(this.type,this.provider),this.supported.api?(this.eventListeners=[],this.listeners=new We(this),this.storage=new Se(this),this.media.plyr=this,x.element(this.elements.container)||(this.elements.container=q("div"),O(this.media,this.elements.container)),Be.migrateStyles.call(this),Be.addStyleHook.call(this),st.setup.call(this),this.config.debug&&ee.call(this,this.elements.container,this.config.events.join(" "),e=>{this.debug.log(`event: ${e.type}`)}),this.fullscreen=new Ve(this),(this.isHTML5||this.isEmbed&&!this.supported.ui)&&Be.build.call(this),this.listeners.container(),this.listeners.global(),this.config.ads.enabled&&(this.ads=new nt(this)),this.isHTML5&&this.config.autoplay&&this.once("canplay",()=>le(this.play())),this.lastSeekTime=0,this.config.previewThumbnails.enabled&&(this.previewThumbnails=new ot(this))):this.debug.error("Setup failed: no support")}get isHTML5(){return this.provider===qe.html5}get isEmbed(){return this.isYouTube||this.isVimeo}get isYouTube(){return this.provider===qe.youtube}get isVimeo(){return this.provider===qe.vimeo}get isVideo(){return this.type===He}get isAudio(){return this.type===De}get playing(){return Boolean(this.ready&&!this.paused&&!this.ended)}get paused(){return Boolean(this.media.paused)}get stopped(){return Boolean(this.paused&&0===this.currentTime)}get ended(){return Boolean(this.media.ended)}set currentTime(e){if(!this.duration)return;const t=x.number(e)&&e>0;this.media.currentTime=t?Math.min(e,this.duration):0,this.debug.log(`Seeking to ${this.currentTime} seconds`)}get currentTime(){return Number(this.media.currentTime)}get buffered(){const{buffered:e}=this.media;return x.number(e)?e:e&&e.length&&this.duration>0?e.end(0)/this.duration:0}get seeking(){return Boolean(this.media.seeking)}get duration(){const e=Number.parseFloat(this.config.duration),t=(this.media||{}).duration,i=x.number(t)&&t!==1/0?t:0;return e||i}set volume(e){let t=e;x.string(t)&&(t=Number(t)),x.number(t)||(t=this.storage.get("volume")),x.number(t)||({volume:t}=this.config),t>1&&(t=1),t<0&&(t=0),this.config.volume=t,this.media.volume=t,!x.empty(e)&&this.muted&&t>0&&(this.muted=!1)}get volume(){return Number(this.media.volume)}set muted(e){let t=e;x.boolean(t)||(t=this.storage.get("muted")),x.boolean(t)||(t=this.config.muted),this.config.muted=t,this.media.muted=t}get muted(){return Boolean(this.media.muted)}get hasAudio(){return!this.isHTML5||(!!this.isAudio||(Boolean(this.media.mozHasAudio)||Boolean(this.media.webkitAudioDecodedByteCount)||Boolean(this.media.audioTracks&&this.media.audioTracks.length)))}set speed(e){let t=null;x.number(e)&&(t=e),x.number(t)||(t=this.storage.get("speed")),x.number(t)||(t=this.config.speed.selected);const{minimumSpeed:i,maximumSpeed:s}=this;t=at(t,i,s),this.config.speed.selected=t,setTimeout(()=>{this.media&&(this.media.playbackRate=t)},0)}get speed(){return Number(this.media.playbackRate)}get minimumSpeed(){return this.isYouTube?Math.min(...this.options.speed):this.isVimeo?.5:.0625}get maximumSpeed(){return this.isYouTube?Math.max(...this.options.speed):this.isVimeo?2:16}set quality(e){const t=this.config.quality,i=this.options.quality;if(!i.length)return;let s=[!x.empty(e)&&Number(e),this.storage.get("quality"),t.selected,t.default].find(x.number),n=!0;if(!i.includes(s)){const e=oe(i,s);this.debug.warn(`Unsupported quality option: ${s}, using ${e} instead`),s=e,n=!1}t.selected=s,this.media.quality=s,n&&this.storage.set({quality:s})}get quality(){return this.media.quality}set loop(e){const t=x.boolean(e)?e:this.config.loop.active;this.config.loop.active=t,this.media.loop=t}get loop(){return Boolean(this.media.loop)}set source(e){ct.change.call(this,e)}get source(){return this.media.currentSrc}get download(){const{download:e}=this.config.urls;return x.url(e)?e:this.source}set download(e){x.url(e)&&(this.config.urls.download=e,xe.setDownloadUrl.call(this))}set poster(e){this.isVideo?Be.setPoster.call(this,e,!1).catch(()=>{}):this.debug.warn("Poster can only be set for video")}get poster(){return this.isVideo?this.media.getAttribute("poster")||this.media.getAttribute("data-poster"):null}get ratio(){if(!this.isVideo)return null;const e=de(me.call(this));return x.array(e)?e.join(":"):e}set ratio(e){this.isVideo?x.string(e)&&he(e)?(this.config.ratio=de(e),pe.call(this)):this.debug.error(`Invalid aspect ratio specified (${e})`):this.debug.warn("Aspect ratio can only be set for video")}set autoplay(e){this.config.autoplay=x.boolean(e)?e:this.config.autoplay}get autoplay(){return Boolean(this.config.autoplay)}toggleCaptions(e){$e.toggle.call(this,e,!1)}set currentTrack(e){$e.set.call(this,e,!1),$e.setup.call(this)}get currentTrack(){const{toggled:e,currentTrack:t}=this.captions;return e?t:-1}set language(e){$e.setLanguage.call(this,e,!1)}get language(){return($e.getCurrentTrack.call(this)||{}).language}set pip(e){if(!J.pip)return;const t=x.boolean(e)?e:!this.pip;x.function(this.media.webkitSetPresentationMode)&&this.media.webkitSetPresentationMode(t?Oe:je),x.function(this.media.requestPictureInPicture)&&(!this.pip&&t?this.media.requestPictureInPicture():this.pip&&!t&&document.exitPictureInPicture())}get pip(){return J.pip?x.empty(this.media.webkitPresentationMode)?this.media===document.pictureInPictureElement:this.media.webkitPresentationMode===Oe:null}setPreviewThumbnails(e){this.previewThumbnails&&this.previewThumbnails.loaded&&(this.previewThumbnails.destroy(),this.previewThumbnails=null),Object.assign(this.config.previewThumbnails,e),this.config.previewThumbnails.enabled&&(this.previewThumbnails=new ot(this))}static supported(e,t){return J.check(e,t)}static loadSprite(e,t){return Pe(e,t)}static setup(e,t={}){let i=null;return x.string(e)?i=Array.from(document.querySelectorAll(e)):x.nodeList(e)?i=Array.from(e):x.array(e)&&(i=e.filter(x.element)),x.empty(i)?null:i.map(e=>new ut(e,t))}}var ht;return ut.defaults=(ht=_e,JSON.parse(JSON.stringify(ht))),ut});
//# sourceMappingURL=plyr.min.js.map


/* The reader's own record of what they have read — and the ONE file that
   touches the store.

   ⛔ **No server, no origin, no network** (R8). A reader who never starts
   `studyforge serve` still keeps their place, because a read mark is the
   reader's own assertion and needs nobody's agreement to be true. ⚠️ The
   served half is a different fact: a PASS is established by a grader run and
   is written where it was established (spec §8.5). ⛔ **A read mark is
   never a pass**, and nothing here can produce one — this file has no notion
   of a practice, a grader or a result at all.

   ⛔ **An explicit act, never inferred.** Nothing here observes scrolling, the
   narration reaching the end, or the page having been opened. The store is
   written when the reader presses the control and at no other time. A record
   the reader cannot trust is worse than none.

   ⛔ **Two records, never one.** The marks and the reader's display
   preferences have different shapes and different lifetimes, so they are two
   storage keys, read and written independently, each carrying its own version
   — and a preference that fails to parse cannot take every mark with it.
   ⚠️ The floor declares no display preference yet; the record exists because
   the INDEPENDENCE is the ruling, and the first consumer is expected to be
   the narration transport (a speed, a volume), which has no business sharing a
   key with the reading record.

   ⛔ **No clock.** A timestamp is a second fact nobody asked for, and it turns
   the personal archive's merge from a set union into an ordering problem. Nothing here
   reads `Date`, and the marks are kept sorted so the stored text is stable
   under re-marking rather than ordered by when somebody pressed a button.

   ⛔ **A stored value this cannot display is discarded, not applied.** A
   record of the wrong shape, an unknown version, an entry that is not a key:
   dropped. ⚠️ A half-applied record is worse than an empty one — the reader
   would be shown marks nobody can account for, under names nothing matches.

   ⚠️ **This part comes BEFORE anything that uses it in `bundle.SCRIPT_PARTS`,
   and the order is asserted against the real composition.** The extraction
   source placed its store AFTER the page script that read it at startup: the
   guard skipped, the setting silently never came back, the suite stayed
   green, and it was found only by loading a page in a browser.

   ⛔ **The key a mark is filed under is minted by `Address.unit_key` in
   Python and carried to this page as data.** Nothing here composes one: a
   second spelling that differed by one character would simply never match
   anything, with nothing failing anywhere. */

(function () {
  'use strict';

  /* The two records. ⛔ The version is in the NAME, not only in the body: a
     record this build cannot read is one it must not overwrite in place
     either, so the next shape is the next key and the old one is left where
     the reader's browser put it. */
  var MARKS_KEY = 'studyforge.read.v1';
  var DISPLAY_KEY = 'studyforge.display.v1';

  /* ⛔ THE BOOT CACHE, AND IT IS A DIFFERENT STORAGE AREA ON PURPOSE.
     ⚠️ `page.html` carries a synchronous boot in the `<head>` so a
     reader who chose a theme is not shown the other one for a frame. A boot is
     the EARLIEST a document can touch a storage area, so it must not read
     `localStorage`: a document that binds it before the previous page's write has
     been committed keeps a snapshot WITHOUT that write, for its whole life.
     ⛔ Under load a mark written on one page is then missing on the next in a
     large share of runs. ⛔ And it is not cosmetic — the reader then marks the page they
     are on, `writeMarks` composes the record from the stale set, and the
     earlier mark is gone.

     ⭐ So what the boot reads is a CACHE in `sessionStorage`, whose area is
     separate: touching it binds nothing the marks live in. ⛔ It is never an
     authority — the display record above is — and nothing here reads it back.
     ⚠️ The key is composed rather than written whole, so a caller names a
     preference and never a key. */
  var BOOT_PREFIX = 'studyforge.boot.';
  var BOOT_SUFFIX = '.v1';

  /* The shape inside a record. ⚠️ Versioned in the body as well, because a
     browser can hold a key this build wrote and a key a later build wrote,
     and a reader whose two machines disagree is the normal case. */
  var RECORD_VERSION = 1;
  var MARKS_FIELD = 'read';
  var DISPLAY_FIELD = 'display';

  /* How long a thing this will keep. ⚠️ A bound rather than a grammar: the
     unit key's grammar is `Address`'s and re-spelling it here would be a
     second definition of what a key is, wrong the day one of them changes.
     What this needs to know is only whether it can display the value. */
  var LONGEST = 200;

  /* Whether the browser will let us keep anything at all. ⚠️ Probed rather
     than assumed: a `file://` page in a private window, or one whose site
     data is blocked, THROWS on the property access itself — not on the
     write — so there is no answer to be had without a try. */
  function backing() {
    try {
      var store = window.localStorage;
      var probe = MARKS_KEY + '.probe';
      store.setItem(probe, '1');
      store.removeItem(probe);
      return store;
    } catch (error) {
      return null;
    }
  }

  var backed = backing();

  /* The same probe against the session area. ⚠️ Probed separately: a browser
     can refuse one and allow the other, and a refused cache costs a frame of
     flash while a refused store costs the reader their marks. */
  function sessioned() {
    try {
      var store = window.sessionStorage;
      var probe = BOOT_PREFIX + 'probe';
      store.setItem(probe, '1');
      store.removeItem(probe);
      return store;
    } catch (error) {
      return null;
    }
  }

  var cached = sessioned();

  /* ⛔ What the head boot may act on, kept for one preference. A value of
     `null` REMOVES it, because an absent cache and a cached word must not be
     two answers to one question: the boot acts on what it finds or on nothing.
     ⭐ Returns whether it took, the way `keep` does. */
  function cache(name, value) {
    if (!cached) { return false; }
    try {
      if (value === null) {
        cached.removeItem(BOOT_PREFIX + name + BOOT_SUFFIX);
      } else {
        cached.setItem(BOOT_PREFIX + name + BOOT_SUFFIX, value);
      }
      return true;
    } catch (error) {
      return false;
    }
  }

  /* One record, or null when there is nothing this can use. ⛔ Every way of
     being unusable lands here and returns the same thing, so a caller never
     sees a half-parsed record: no store, no entry, not JSON, not an object,
     a version this build does not know. */
  function record(name) {
    if (!backed) { return null; }
    var raw;
    try { raw = backed.getItem(name); } catch (error) { return null; }
    if (raw === null) { return null; }
    var parsed;
    try { parsed = JSON.parse(raw); } catch (error) { return null; }
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) { return null; }
    if (parsed.version !== RECORD_VERSION) { return null; }
    return parsed;
  }

  function keep(name, body) {
    if (!backed) { return false; }
    try {
      backed.setItem(name, JSON.stringify(body));
      return true;
    } catch (error) {
      /* A quota refusal, or site data blocked between the probe and now. ⛔
         Reported as a failed write rather than swallowed, so the control
         reads the store back and shows what is actually there. */
      return false;
    }
  }

  /* Whether this is something the control could show a reader, and a bound
     rather than a grammar. ⚠️ Written as a loop over code points rather than
     as a character class, because a HYPHEN inside one is a range operator or a literal depending
     on where it sits — and every slug this framework mints is hyphenated,
     so a class that swallowed `-` would discard every key there is.
     ⛔ Refused: anything at or below a space (every ASCII control and every
     space), and DEL. A key this cannot display is discarded, not applied. */
  function usable(value) {
    if (typeof value !== 'string' || value.length === 0 || value.length > LONGEST) {
      return false;
    }
    for (var at = 0; at < value.length; at += 1) {
      var code = value.charCodeAt(at);
      if (code <= 0x20 || code === 0x7f) { return false; }
    }
    return true;
  }

  /* The marks, sorted, with anything unusable dropped. ⛔ Dropped and not
     repaired: there is no shape a bad entry could be corrected INTO that the
     reader ever asserted. */
  function marks() {
    var held = record(MARKS_KEY);
    var listed = held && Array.isArray(held[MARKS_FIELD]) ? held[MARKS_FIELD] : [];
    var kept = [];
    listed.forEach(function (entry) {
      if (usable(entry) && kept.indexOf(entry) === -1) { kept.push(entry); }
    });
    return kept.sort();
  }

  function writeMarks(kept) {
    var body = { version: RECORD_VERSION };
    body[MARKS_FIELD] = kept.slice().sort();
    return keep(MARKS_KEY, body);
  }

  function marked(key) {
    return usable(key) && marks().indexOf(key) !== -1;
  }

  function mark(key) {
    if (!usable(key)) { return false; }
    var kept = marks();
    if (kept.indexOf(key) === -1) { kept.push(key); }
    return writeMarks(kept);
  }

  function unmark(key) {
    if (!usable(key)) { return false; }
    return writeMarks(marks().filter(function (held) { return held !== key; }));
  }

  /* The second record, and it is deliberately the same machinery over a
     different key — never the same record with a second field in it. */
  function preferences() {
    var held = record(DISPLAY_KEY);
    var kept = held && held[DISPLAY_FIELD] && typeof held[DISPLAY_FIELD] === 'object'
      ? held[DISPLAY_FIELD] : {};
    var answer = {};
    Object.keys(kept).sort().forEach(function (name) {
      if (usable(name) && usable(kept[name])) { answer[name] = kept[name]; }
    });
    return answer;
  }

  function preference(name) {
    var held = preferences();
    return Object.prototype.hasOwnProperty.call(held, name) ? held[name] : null;
  }

  function prefer(name, value) {
    if (!usable(name) || !usable(value)) { return false; }
    var held = preferences();
    held[name] = value;
    var body = { version: RECORD_VERSION };
    body[DISPLAY_FIELD] = held;
    return keep(DISPLAY_KEY, body);
  }

  /* ⛔ Published under one name, so the unit page, the container page and the
     root index share one implementation. Two would be a mark written under
     one name and read back under another, with no symptom but a badge that
     never lights. */
  window.studyforge = window.studyforge || {};
  window.studyforge.progress = {
    MARKS_KEY: MARKS_KEY,
    DISPLAY_KEY: DISPLAY_KEY,
    supported: function () { return backed !== null; },
    marks: marks,
    marked: marked,
    mark: mark,
    unmark: unmark,
    preferences: preferences,
    preference: preference,
    prefer: prefer,
    cache: cache
  };
}());

/* The reader's choice of theme: light, dark, or whatever their system says.

   ⛔ **BOTH THEMES ARE REACHABLE FROM THE PAGE.** `palette.css` carries both,
   behind the guards `[data-theme="light"]` and `[data-theme="dark"]`, and this
   control writes one of them, so a reader whose system says light can still
   read the dark page.

   ⛔ **THREE STATES, AND THE THIRD IS THE DEFAULT.** *System* is not the same
   answer as *light*: a reader whose machine turns dark at sunset wants the page
   to follow, and a two-state control can only record "dark" or "not dark" —
   which freezes the page at whatever it was when they pressed it. So *system*
   is a stored value like the others, and it is also what an absent record
   means, so the two can never disagree.

   ⛔ **The store is `study-progress.js`'s display record, not a second one.**
   That file owns every read and every write; this one asks it questions, the
   way `read-mark.js` does. ⚠️ Its docstring already ruled the display record
   independent of the read marks *"because a preference that fails to parse
   cannot take every mark with it"* — this is that record's first consumer.
   ⛔ Read through the published name with NO existence guard, and this part
   sits after `study-progress.js` in `bundle.SCRIPT_PARTS` for that reason.

   ⛔ **The control is NOT gated on storage working.** `read-mark.js` hides its
   control when nothing can be stored, because a mark that is not kept is a lie.
   A theme that is not kept is still a theme: the reader sees the page they
   asked for, for as long as they are on it. ⚠️ So this shows the control
   whenever scripting is on, and `prefer()` returning false costs the page
   nothing it was showing.

   ⛔ **THE FLASH IS PREVENTED IN THE HEAD, NOT HERE.** This part is deferred,
   so it runs after the first paint — applying the stored theme here would show
   every reader the wrong page for a frame. `page.html` carries a tiny
   synchronous boot in `<head>` that sets `data-theme` before anything is
   painted.

   ⛔ **AND THAT BOOT READS `sessionStorage`, NEVER `localStorage`, WHICH IS
   A CORRECTNESS RULE RATHER THAN A PREFERENCE.** A boot in the `<head>` is the
   document's FIRST touch of whatever storage it reads, and a boot reading
   `localStorage` there loses marks under load: a mark written on one page is
   missing on the next in a large share of runs. ⛔ A document that binds
   the area before the previous document's write has been committed gets a
   snapshot WITHOUT it, and that snapshot is what it keeps: the value was still
   missing a second later. ⛔ **The harm is not cosmetic** — the reader then
   presses *Mark as read* on that page, `writeMarks` composes the new record
   from the stale set, and the earlier mark is destroyed: two marks can end as
   `{"version":1,"read":[]}`.

   ⭐ **So the boot reads a CACHE in `sessionStorage`, which is a different
   storage area and binds nothing in `localStorage`.** ⛔ The cache is the
   STORE's — `progress.cache(name, value)` — because one part touches the
   browser's storage and that does not stop being true because the area is a
   different one. This part asks for it from its own paint, by which time the
   bundle has long since bound the durable area. ⚠️ `sessionStorage` is per tab,
   so the FIRST page opened in a new tab has no cache and paints the system
   scheme for one frame before this part corrects it; every navigation after it
   is flash-free. ⛔ That one frame is the price of not losing a reader's marks,
   and it is stated rather than hidden. ⭐ The durable answer is still the
   display record, which this part alone reads; the cache is never an authority
   and nothing reads it back. ⚠️ The boot spells the cache's key a second time,
   because it must run before any bundle exists; `test_theme` holds the two
   spellings equal, both ways, and refuses a boot that names `localStorage`.

   ⭐ **The words a reader sees are in `page.html`** (R13): this file toggles
   `hidden` and `aria-pressed` and types nothing.

   ⛔ **`theme-color` is kept honest.** The page ships two of them, one per
   system scheme. A reader who has CHOSEN a theme has made those media queries
   wrong, so the chosen one is widened to `all` and the other is switched off;
   choosing *system* puts both media queries back exactly as the skeleton wrote
   them. */

(function () {
  'use strict';

  /* The name the choice is filed under inside the display record, and the three
     values it may take. ⚠️ `SYSTEM` is stored like the others and is also what
     an absent record means. */
  var PREFERENCE = 'theme';
  var LIGHT = 'light';
  var DARK = 'dark';
  var SYSTEM = 'system';

  /* The attribute `palette.css` guards on, and the control's own hooks. ⚠️
     Spelled here and in `render/templates/page.html`, the two-sided spelling
     every hook on this page has: markup and script cannot import one another. */
  var THEME = 'data-theme';
  var CONTROL = '[data-section="theme"]';
  var CHOICE = 'data-theme-choice';

  /* The browser-chrome colour, one per system scheme, and what switches one
     off. ⚠️ `not all` rather than removing the element: the skeleton's own two
     media queries are what *system* restores, so nothing is thrown away. */
  var COLOUR = 'meta[name="theme-color"]';
  var EVERY = 'all';
  var NONE = 'not all';


  var store = window.studyforge.progress;
  var root = document.documentElement;

  var control = document.querySelector(CONTROL);
  if (!control) { return; }
  var buttons = [].slice.call(control.querySelectorAll('[' + CHOICE + ']'));
  if (!buttons.length) { return; }

  /* Each theme-colour element with the media query the skeleton gave it, read
     once, before anything here has had a chance to change one. */
  var colours = [].slice.call(document.querySelectorAll(COLOUR)).map(function (meta) {
    return { meta: meta, media: meta.getAttribute('media') || EVERY };
  });

  /* A stored value this cannot apply is treated as no choice at all — the same
     ruling the store itself makes about a record it cannot display. */
  function chosen() {
    var held = store.preference(PREFERENCE);
    return held === LIGHT || held === DARK ? held : SYSTEM;
  }

  /* ⛔ The cache the head boot reads, kept in step with every paint and kept by
     the STORE rather than by this part. ⚠️ One part touches the browser's
     storage (`test_progress` asserts it), and that does not stop being true
     because the area is a different one. ⭐ *System* caches NOTHING: an absent
     cache and a cached word must not be two answers to one question. */
  function remember(choice) {
    store.cache(PREFERENCE, choice === SYSTEM ? null : choice);
  }

  function paint(choice) {
    if (choice === SYSTEM) {
      root.removeAttribute(THEME);
    } else {
      root.setAttribute(THEME, choice);
    }
    remember(choice);
    colours.forEach(function (carried) {
      if (choice === SYSTEM) {
        carried.meta.setAttribute('media', carried.media);
      } else {
        carried.meta.setAttribute(
          'media',
          carried.media.indexOf(choice) === -1 ? NONE : EVERY
        );
      }
    });
    buttons.forEach(function (button) {
      var mine = button.getAttribute(CHOICE) === choice;
      button.setAttribute('aria-pressed', mine ? 'true' : 'false');
    });
  }

  /* ⛔ Shown from the STORE's answer, never from what was just pressed: a write
     the browser refused must not leave the page claiming it was remembered. */
  function choose(choice) {
    store.prefer(PREFERENCE, choice);
    paint(chosen());
  }

  paint(chosen());
  control.hidden = false;

  buttons.forEach(function (button) {
    button.addEventListener('click', function () {
      choose(button.getAttribute(CHOICE));
    });
  });
}());

/* The copy button on a code block.

   ⛔ Progressive enhancement, and it is the reason the button is created HERE
   rather than rendered into the page. A button written into the markup would
   sit there doing nothing with scripting off, and a control that does nothing
   is worse than no control. The page ships the figure; this adds the button
   when there is something to add it to.

   ⚠️ `navigator.clipboard` needs a secure context, which `file://` is not in
   every browser. The fallback is the old selection-and-`execCommand` route,
   and when neither works the button says so instead of silently doing
   nothing. */

(function () {
  /* ⛔ The fallback names the key THIS machine uses: it said
     "Press ⌘C" to every reader, which is wrong everywhere but a Mac. */
  function copyKey() {
    var platform = (navigator.userAgentData && navigator.userAgentData.platform) ||
      navigator.platform || '';
    return /mac|iphone|ipad/i.test(platform) ? '\u2318C' : 'Ctrl+C';
  }

  var figures = [].slice.call(document.querySelectorAll('figure.code'));
  if (!figures.length) { return; }

  function copy(text) {
    if (navigator.clipboard && window.isSecureContext) {
      return navigator.clipboard.writeText(text);
    }
    return new Promise(function (resolve, reject) {
      var area = document.createElement('textarea');
      area.value = text;
      area.setAttribute('readonly', '');
      area.style.position = 'fixed';
      area.style.left = '-9999px';
      document.body.appendChild(area);
      area.select();
      var ok = false;
      try { ok = document.execCommand('copy'); } catch (error) { ok = false; }
      document.body.removeChild(area);
      if (ok) { resolve(); } else { reject(new Error('copy refused')); }
    });
  }

  figures.forEach(function (figure) {
    var code = figure.querySelector('pre code');
    var caption = figure.querySelector('figcaption');
    if (!code || !caption) { return; }

    var button = document.createElement('button');
    button.type = 'button';
    button.className = 'copy';
    button.textContent = 'Copy code';
    caption.appendChild(button);

    button.addEventListener('click', function () {
      copy(code.textContent).then(function () {
        button.textContent = 'Copied';
      }, function () {
        button.textContent = 'Select it and press ' + copyKey();
      });
      window.setTimeout(function () { button.textContent = 'Copy code'; }, 2000);
    });
  });
}());

/* The video block's controls, and the one place Plyr is stopped from
   reaching the network.

   Plyr (vendored, `plyr.js`) enhances the `<video>` the page already shipped,
   so NO generated page changes and the renderer's markup is untouched — the
   same bargain the syntax highlighting strikes. With JS off the element keeps
   its native `controls` attribute and still plays.

   ⛔ NOTHING HERE MAY REACH THE NETWORK (R8). The site is read over `file://`
   as well as from a server, and a clone is meant to play its material with no
   connection at all. Plyr would otherwise fetch its icon sprite from a CDN on
   every single init, so the sprite is vendored, injected into the document
   once, and `loadSprite` is off with `iconUrl` empty — which makes Plyr emit
   `<use href="#plyr-play">` against the inline copy. `blankVideo` is emptied
   for the same reason. ⚠️ The vendored bundle still CONTAINS remote URLs, for
   the streaming providers this site never uses; what matters is that no code
   path here can reach one, and `test_no_network` asserts each disarming
   option is set rather than that the bundle is free of strings. */

(function () {
  var videos = [].slice.call(document.querySelectorAll('figure.video video'));
  if (!videos.length || typeof Plyr === 'undefined') { return; }

  /* Substituted from the vendored `plyr.svg` when the script bundle is
     composed, so the sprite has exactly one source on disk and no derived
     file is committed beside it. */
  var SPRITE = '<?xml version="1.0" encoding="UTF-8"?><!-- Plyr 3.8.4 -- plyr.io -- MIT, see plyr.LICENSE beside this file. VENDORED, UNMODIFIED apart from this header. Do not edit: re-vendor with `npm pack plyr` and take dist/plyr.svg. Injected into the page by video-player.js so no icon is ever fetched (R8). --><!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"><svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><symbol id="plyr-airplay" viewBox="0 0 18 18"><path d="M16 1H2a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h3v-2H3V3h12v8h-2v2h3a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1"/><path d="M4 17h10l-5-6z"/></symbol><symbol id="plyr-captions-off" viewBox="0 0 18 18"><path fill-opacity=".5" fill-rule="evenodd" d="M1 1c-.6 0-1 .4-1 1v11c0 .6.4 1 1 1h4.6l2.7 2.7c.2.2.4.3.7.3s.5-.1.7-.3l2.7-2.7H17c.6 0 1-.4 1-1V2c0-.6-.4-1-1-1zm4.52 10.15c1.99 0 3.01-1.32 3.28-2.41l-1.29-.39c-.19.66-.78 1.45-1.99 1.45-1.14 0-2.2-.83-2.2-2.34 0-1.61 1.12-2.37 2.18-2.37 1.23 0 1.78.75 1.95 1.43l1.3-.41C8.47 4.96 7.46 3.76 5.5 3.76c-1.9 0-3.61 1.44-3.61 3.7s1.65 3.69 3.63 3.69m7.57 0c1.99 0 3.01-1.32 3.28-2.41l-1.29-.39c-.19.66-.78 1.45-1.99 1.45-1.14 0-2.2-.83-2.2-2.34 0-1.61 1.12-2.37 2.18-2.37 1.23 0 1.78.75 1.95 1.43l1.3-.41c-.28-1.15-1.29-2.35-3.25-2.35-1.9 0-3.61 1.44-3.61 3.7s1.65 3.69 3.63 3.69"/></symbol><symbol id="plyr-captions-on" viewBox="0 0 18 18"><path fill-rule="evenodd" d="M1 1c-.6 0-1 .4-1 1v11c0 .6.4 1 1 1h4.6l2.7 2.7c.2.2.4.3.7.3s.5-.1.7-.3l2.7-2.7H17c.6 0 1-.4 1-1V2c0-.6-.4-1-1-1zm4.52 10.15c1.99 0 3.01-1.32 3.28-2.41l-1.29-.39c-.19.66-.78 1.45-1.99 1.45-1.14 0-2.2-.83-2.2-2.34 0-1.61 1.12-2.37 2.18-2.37 1.23 0 1.78.75 1.95 1.43l1.3-.41C8.47 4.96 7.46 3.76 5.5 3.76c-1.9 0-3.61 1.44-3.61 3.7s1.65 3.69 3.63 3.69m7.57 0c1.99 0 3.01-1.32 3.28-2.41l-1.29-.39c-.19.66-.78 1.45-1.99 1.45-1.14 0-2.2-.83-2.2-2.34 0-1.61 1.12-2.37 2.18-2.37 1.23 0 1.78.75 1.95 1.43l1.3-.41c-.28-1.15-1.29-2.35-3.25-2.35-1.9 0-3.61 1.44-3.61 3.7s1.65 3.69 3.63 3.69"/></symbol><symbol id="plyr-download" viewBox="0 0 18 18"><path d="M9 13c.3 0 .5-.1.7-.3L15.4 7 14 5.6l-4 4V1H8v8.6l-4-4L2.6 7l5.7 5.7c.2.2.4.3.7.3m-7 2h14v2H2z"/></symbol><symbol id="plyr-enter-fullscreen" viewBox="0 0 18 18"><path d="M10 3h3.6l-4 4L11 8.4l4-4V8h2V1h-7zM7 9.6l-4 4V10H1v7h7v-2H4.4l4-4z"/></symbol><symbol id="plyr-exit-fullscreen" viewBox="0 0 18 18"><path d="M1 12h3.6l-4 4L2 17.4l4-4V17h2v-7H1zM16 .6l-4 4V1h-2v7h7V6h-3.6l4-4z"/></symbol><symbol id="plyr-fast-forward" viewBox="0 0 18 18"><path d="M7.875 7.171 0 1v16l7.875-6.171V17L18 9 7.875 1z"/></symbol><symbol id="plyr-logo-vimeo" viewBox="0 0 18 18"><path d="M17 5.3c-.1 1.6-1.2 3.7-3.3 6.4-2.2 2.8-4 4.2-5.5 4.2-.9 0-1.7-.9-2.4-2.6C5 10.9 4.4 6 3 6c-.1 0-.5.3-1.2.8l-.8-1c.8-.7 3.5-3.4 4.7-3.5S7.7 3 8 4.8c.3 2 .8 6.1 1.8 6.1.9 0 2.5-3.4 2.6-4 .1-.9-.3-1.9-2.3-1.1.8-2.6 2.3-3.8 4.5-3.8q2.55.15 2.4 3.3"/></symbol><symbol id="plyr-logo-youtube" viewBox="0 0 18 18"><path d="M16.8 5.8c-.2-1.3-.8-2.2-2.2-2.4C12.4 3 9 3 9 3s-3.4 0-5.6.4C2 3.6 1.3 4.5 1.2 5.8 1 7.1 1 9 1 9s0 1.9.2 3.2.8 2.2 2.2 2.4C5.6 15 9 15 9 15s3.4 0 5.6-.4c1.4-.3 2-1.1 2.2-2.4S17 9 17 9s0-1.9-.2-3.2M7 12V6l5 3z"/></symbol><symbol id="plyr-muted" viewBox="0 0 18 18"><path d="m12.4 12.5 2.1-2.1 2.1 2.1 1.4-1.4L15.9 9 18 6.9l-1.4-1.4-2.1 2.1-2.1-2.1L11 6.9 13.1 9 11 11.1zM3.786 6.008H.714C.286 6.008 0 6.31 0 6.76v4.512c0 .452.286.752.714.752h3.072l4.071 3.858c.5.3 1.143 0 1.143-.602V2.752c0-.601-.643-.977-1.143-.601z"/></symbol><symbol id="plyr-pause" viewBox="0 0 18 18"><path d="M6 1H3c-.6 0-1 .4-1 1v14c0 .6.4 1 1 1h3c.6 0 1-.4 1-1V2c0-.6-.4-1-1-1m6 0c-.6 0-1 .4-1 1v14c0 .6.4 1 1 1h3c.6 0 1-.4 1-1V2c0-.6-.4-1-1-1z"/></symbol><symbol id="plyr-pip" viewBox="0 0 18 18"><path d="M13.293 3.293 7.022 9.564l1.414 1.414 6.271-6.271L17 7V1h-6z"/><path d="M13 15H3V5h5V3H2a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-6h-2z"/></symbol><symbol id="plyr-play" viewBox="0 0 18 18"><path d="M15.562 8.1 3.87.225c-.818-.562-1.87 0-1.87.9v15.75c0 .9 1.052 1.462 1.87.9L15.563 9.9c.584-.45.584-1.35 0-1.8"/></symbol><symbol id="plyr-restart" viewBox="0 0 18 18"><path d="m9.7 1.2.7 6.4 2.1-2.1c1.9 1.9 1.9 5.1 0 7-.9 1-2.2 1.5-3.5 1.5s-2.6-.5-3.5-1.5c-1.9-1.9-1.9-5.1 0-7 .6-.6 1.4-1.1 2.3-1.3l-.6-1.9C6 2.6 4.9 3.2 4 4.1 1.3 6.8 1.3 11.2 4 14c1.3 1.3 3.1 2 4.9 2 1.9 0 3.6-.7 4.9-2 2.7-2.7 2.7-7.1 0-9.9L16 1.9z"/></symbol><symbol id="plyr-rewind" viewBox="0 0 18 18"><path d="M10.125 1 0 9l10.125 8v-6.171L18 17V1l-7.875 6.171z"/></symbol><symbol id="plyr-settings" viewBox="0 0 18 18"><path d="M16.135 7.784a2 2 0 0 1-1.23-2.969c.322-.536.225-.998-.094-1.316l-.31-.31c-.318-.318-.78-.415-1.316-.094a2 2 0 0 1-2.969-1.23C10.065 1.258 9.669 1 9.219 1h-.438c-.45 0-.845.258-.997.865a2 2 0 0 1-2.969 1.23c-.536-.322-.999-.225-1.317.093l-.31.31c-.318.318-.415.781-.093 1.317a2 2 0 0 1-1.23 2.969C1.26 7.935 1 8.33 1 8.781v.438c0 .45.258.845.865.997a2 2 0 0 1 1.23 2.969c-.322.536-.225.998.094 1.316l.31.31c.319.319.782.415 1.316.094a2 2 0 0 1 2.969 1.23c.151.607.547.865.997.865h.438c.45 0 .845-.258.997-.865a2 2 0 0 1 2.969-1.23c.535.321.997.225 1.316-.094l.31-.31c.318-.318.415-.781.094-1.316a2 2 0 0 1 1.23-2.969c.607-.151.865-.547.865-.997v-.438c0-.451-.26-.846-.865-.997M9 12a3 3 0 1 1 0-6 3 3 0 0 1 0 6"/></symbol><symbol id="plyr-volume" viewBox="0 0 18 18"><path d="M15.6 3.3c-.4-.4-1-.4-1.4 0s-.4 1 0 1.4C15.4 5.9 16 7.4 16 9s-.6 3.1-1.8 4.3c-.4.4-.4 1 0 1.4.2.2.5.3.7.3.3 0 .5-.1.7-.3C17.1 13.2 18 11.2 18 9s-.9-4.2-2.4-5.7"/><path d="M11.282 5.282a.91.91 0 0 0 0 1.316c.735.735.995 1.458.995 2.402 0 .936-.425 1.917-.995 2.487a.91.91 0 0 0 0 1.316c.145.145.636.262 1.018.156a.7.7 0 0 0 .298-.156C13.773 11.733 14.13 10.16 14.13 9q.001-.255-.011-.51c-.053-.992-.319-2.005-1.522-3.208a.91.91 0 0 0-1.316 0m-7.495.726H.714C.286 6.008 0 6.31 0 6.76v4.512c0 .452.286.752.714.752h3.072l4.071 3.858c.5.3 1.143 0 1.143-.602V2.752c0-.601-.643-.977-1.143-.601z"/></symbol></svg>';

  var holder = document.createElement('div');
  holder.hidden = true;
  holder.setAttribute('aria-hidden', 'true');
  holder.innerHTML = SPRITE;
  document.body.insertBefore(holder, document.body.firstChild);

  var players = videos.map(function (video) {
    return new Plyr(video, {
      /* Plyr's own shortcuts, only once the player has focus. The page-wide
         ones below are ours, and are deliberately narrower. */
      keyboard: { focused: true, global: false },
      loadSprite: false,
      iconUrl: '',
      blankVideo: '',
      speed: { selected: 1, options: [0.5, 0.75, 1, 1.25, 1.5, 1.75, 2] },
      seekTime: 5,
      controls: [
        'play-large', 'restart', 'rewind', 'play', 'fast-forward', 'progress',
        'current-time', 'duration', 'mute', 'volume', 'settings', 'pip',
        'fullscreen'
      ],
      settings: ['speed'],
      tooltips: { controls: true, seek: true }
    });
  });

  /* Two videos on one page must not speak over each other. Wired from the
     elements' own events rather than from either player's internals, so
     neither has to know the other exists — and so narration can join
     the same convention without either side being edited. */
  videos.forEach(function (video) {
    video.addEventListener('play', function () {
      videos.forEach(function (other) { if (other !== video) { other.pause(); } });
    });
  });

  function playing() {
    for (var i = 0; i < videos.length; i += 1) {
      if (!videos[i].paused && !videos[i].ended) { return videos[i]; }
    }
    return null;
  }

  /* While a video is PLAYING it owns space and the arrows, wherever focus
     sits; when it stops they go back untouched.

     ⛔ Registered in the CAPTURE phase on `document`, which is what lets this
     stay purely additive: a later bubble-phase listener on the same node
     never runs while a video is playing, so no other script needs editing and
     none needs to know this file exists. */
  document.addEventListener('keydown', function (event) {
    if (event.metaKey || event.ctrlKey || event.altKey) { return; }
    var video = playing();
    if (!video) { return; }
    var tag = (event.target && event.target.tagName || '').toLowerCase();
    if (tag === 'input' || tag === 'textarea' || tag === 'select') { return; }

    var handled = true;
    if (event.key === ' ' || event.key === 'k') { video.pause(); }
    else if (event.key === 'ArrowRight' || event.key === 'l') {
      video.currentTime = Math.min(video.duration || Infinity, video.currentTime + 5);
    } else if (event.key === 'ArrowLeft' || event.key === 'j') {
      video.currentTime = Math.max(0, video.currentTime - 5);
    } else if (event.key === 'ArrowUp') {
      video.volume = Math.min(1, video.volume + 0.1);
    } else if (event.key === 'ArrowDown') {
      video.volume = Math.max(0, video.volume - 0.1);
    } else { handled = false; }

    if (handled) {
      event.preventDefault();
      event.stopPropagation();
    }
  }, true);

  window.__studyforgeVideoPlayers = players;
}());

/* The narration transport: play, advance, and the highlight that tracks what is spoken.

   ⛔ **One clip per speech unit, and the granularity is the whole design.**
   Spec §8.2 chose the speech unit precisely so a highlight can track playback with
   no word-level timing data anywhere: the audio element already knows which clip
   it is playing, so *which passage is lit* is the same question as *which clip is
   loaded*, and no timing table has to be kept in step with anything.

   ⛔ **Nothing here composes a clip path.** A passage carries its own source,
   emitted by the renderer from `corpus.placement`'s answer (R4) — `audio/x.mp3`
   under the `tree` profile and `audio/<stem>/x.mp3` under `sibling`. ⚠️ A script
   that spelled either would be correct under one profile and silently wrong under
   the other, and the page would render both ways.

   ⛔ **Nothing here types a word a reader sees** (R13). Every sentence is in
   `templates/player.html`, carried as a hidden `[data-state]` span this file
   unhides — the same shape `read-mark.js` uses, and for the same reason: a
   sentence in a script is a sentence no template check reads and no translator
   finds. ⭐ The two things written into the page are a **count** and a **heading
   copied off the page itself**; neither is prose this file authored.

   ⛔ **Progressive enhancement, and the transport ships HIDDEN.** With scripting
   off, a reader is shown nothing rather than a Play button that cannot play —
   a control that does nothing is a dead control, and none is shown. ⭐ The same
   holds when the clips are not on disk: the transport stays hidden, and this
   file learns it without requesting a clip (see `CLIPS` below).

   ⭐ **It degrades honestly, in three named states** (R6). A passage whose clip
   is not on disk says so and stops rather than pretending; a unit with no usable
   clip at all says so and the controls are disabled; a browser refusing to start
   audio is a known state with a stated remedy — press play once — and is neither
   an error nor hidden.

   ⛔ **No network** (R8). The only thing this file assigns to `src` is a value it
   read off the page, which is relative to the page by construction. */

(function () {
  'use strict';

  /* The region and the transport, both from `templates/player.html`. ⚠️ Spelled
     here and in the template, which is the two-sided spelling every hook on this
     page has: markup and script cannot import one another. */
  var PLAYER = 'player';
  var NARRATOR = 'narrator';

  /* What a narrated passage carries. ⛔ `data-audio` is `render/page/assets.py`'s
     `AUDIO_ATTRIBUTE`, named there one milestone before its writer so the two
     sides could not spell it differently — and it holds the page-relative HREF of
     the clip, which is what a module whose whole subject is *where this page
     reaches* declares.

     ⚠️ **`data-speech-id` is deliberately NOT read here.** The positional id is
     what a *structure* edit must not renumber (spec §8.2), and the player has no
     question it answers: which clip is loaded already says which passage is lit.
     ⛔ Reading it to key something the DOM order already keys would be a second
     ordering, agreeing today and disagreeing the day one of them is wrong. */
  var SOURCE = 'data-audio';

  /* The highlight. ⭐ NOT published in `pageassets.SURFACE_HOOKS`, and that is a
     decision rather than an omission: `data-marked` is published because
     `chrome.css` paints a state `read-mark.js` writes, so two parts hold the
     two ends. Here `narration.css` and this file are one feature's, the spelling
     has one owner, and publishing it would oblige a stylesheet nobody else writes. */
  var SPEAKING = 'data-speaking';

  /* Which sentence, and which face of the play button, is showing. */
  var STATE = 'data-state';
  var MISSING = 'missing';
  var BLOCKED = 'blocked';
  var NONE = 'none';
  var PAUSED = 'paused';
  var PLAYING = 'playing';

  /* How the fill draws itself. ⭐ A custom property rather than a width, so
     `narration.css` owns the drawing and this file only ever states a number. */
  var PROGRESS = '--progress';

  var player = document.getElementById(PLAYER);
  var audio = document.getElementById(NARRATOR);
  if (!player || !audio) { return; }

  /* ⛔ **Whether the clips are on disk is asked of a script that is always
     there, never of a clip.** `templates/player.html` links
     `pageassets.CLIPS_NAME` ahead of this bundle, and it says `present` only
     where a build found clips or a restore put them back. ⚠️ A request for a
     clip that is not there is an error in the console, over `file://` and
     served alike, and a site whose clips are a download nobody has taken is the
     normal case. ⭐ So anything but `present` leaves the transport hidden and
     binds nothing: no button, no passage that answers a click, no key. */
  var CLIPS = 'present';
  var told = window.studyforge && window.studyforge.clips;
  if (told !== CLIPS) { return; }

  /* ⛔ THE WHOLE DOCUMENT, NOT `#content`. A unit page is headed by its
     material's own opening heading, and that heading sits in the `<header>`
     above the content — it is a narrated passage like every other one. Scoped to
     `#content` the transport would skip the first passage of every page while
     `speakable` still made its clip: a clip on disk that nothing could play. ⭐ `querySelectorAll` answers in
     document order, so the heading is still passage one. ⚠️ Nothing outside the
     heading and the content carries `data-audio` — `render/page/document.py` is
     the one composer of this skeleton and fills the attribute in exactly those
     two places. */
  var passages = [].slice.call(document.querySelectorAll('[' + SOURCE + ']'));
  if (!passages.length) { return; }

  var track = document.getElementById('track');
  var fill = document.getElementById('fill');
  var previous = document.getElementById('previous');
  var play = document.getElementById('play');
  var next = document.getElementById('next');
  var speed = document.getElementById('speed');
  var where = document.getElementById('where');
  var counter = document.getElementById('counter');
  var status = document.getElementById('status');

  /* `state name -> the span carrying that sentence`. ⛔ Read out of the markup,
     never listed here: a state this file knows and the template does not is a
     state that announces nothing, silently. */
  var sentences = faces(status);
  var faceOfPlay = faces(play);

  /* Whether each passage can be played at all. ⚠️ A passage may arrive with an
     empty source — the renderer emitted the attribute and synthesis has not run —
     and a passage may fail to load when its clip is not on disk. Both are the
     same answer to the reader and are kept in one place. */
  var playable = passages.map(function (passage) {
    return !!(passage.getAttribute(SOURCE) || '').trim();
  });

  var at = firstPlayable();
  var reduced = quiet();

  function faces(holder) {
    var found = {};
    if (!holder) { return found; }
    [].slice.call(holder.querySelectorAll('[' + STATE + ']')).forEach(function (face) {
      found[face.getAttribute(STATE)] = face;
    });
    return found;
  }

  /* ⛔ Shown by name, and every other face hidden — so two sentences can never be
     on screen at once and `null` is a legal argument meaning *say nothing*. */
  function say(name) {
    Object.keys(sentences).forEach(function (key) {
      sentences[key].hidden = key !== name;
    });
    /* ⚠️ The region too, not only its sentences: an empty live region left in
       the flow reserves a line that reads as a message which failed to arrive,
       and CSS cannot ask "are all my children hidden" without `:has`. */
    if (status) { status.hidden = !name; }
  }

  function showFace(name) {
    Object.keys(faceOfPlay).forEach(function (key) {
      faceOfPlay[key].hidden = key !== name;
    });
  }

  function quiet() {
    if (!window.matchMedia) { return false; }
    var query = window.matchMedia('(prefers-reduced-motion: reduce)');
    return !!(query && query.matches);
  }

  function firstPlayable() {
    for (var index = 0; index < passages.length; index += 1) {
      if (playable[index]) { return index; }
    }
    return -1;
  }

  function nextPlayable(from, step) {
    for (var index = from + step; index >= 0 && index < passages.length; index += step) {
      if (playable[index]) { return index; }
    }
    return -1;
  }

  function anyPlayable() {
    return firstPlayable() !== -1;
  }

  /* The count, and the heading this passage sits under. ⛔ Both are numbers or
     text already on the page — this file authors neither. */
  function label() {
    if (counter) {
      counter.textContent = at === -1 ? '' : (at + 1) + ' / ' + passages.length;
    }
    if (!where) { return; }
    var heading = at === -1 ? null : headingAbove(passages[at]);
    where.textContent = heading ? heading.textContent : '';
  }

  function headingAbove(passage) {
    var section = passage.closest ? passage.closest('section') : null;
    return section ? section.querySelector('h1, h2, h3, h4, h5, h6') : null;
  }

  /* Overall progress through the unit, not through one clip: the aria-label on
     `#track` says *"Progress through this unit"*, and a bar that reset at every
     passage would be answering a question nobody asked. */
  function progress() {
    var whole = passages.length;
    var done = at === -1 ? 0 : at;
    var within = 0;
    if (audio.duration && isFinite(audio.duration) && audio.duration > 0) {
      within = Math.min(1, (audio.currentTime || 0) / audio.duration);
    }
    var fraction = whole ? Math.min(1, (done + within) / whole) : 0;
    var percent = Math.round(fraction * 1000) / 10;
    if (fill && fill.style && fill.style.setProperty) {
      fill.style.setProperty(PROGRESS, percent + '%');
    }
    if (track) {
      track.setAttribute('aria-valuemax', '100');
      track.setAttribute('aria-valuenow', String(percent));
    }
  }

  function highlight() {
    passages.forEach(function (passage, index) {
      if (index === at) {
        passage.setAttribute(SPEAKING, 'true');
      } else {
        passage.removeAttribute(SPEAKING);
      }
    });
  }

  function reveal(passage) {
    if (!passage || !passage.scrollIntoView) { return; }
    passage.scrollIntoView({ block: 'center', behavior: reduced ? 'auto' : 'smooth' });
  }

  /* ⛔ The one place `src` is assigned, and the value is the page's own. */
  function load(index) {
    at = index;
    audio.src = passages[index].getAttribute(SOURCE);
    audio.playbackRate = rate();
    highlight();
    label();
    progress();
  }

  function rate() {
    var chosen = speed ? parseFloat(speed.value) : 1;
    return chosen > 0 ? chosen : 1;
  }

  /* ⛔ ONLY the rejection is handled, and that is not laziness — it is a defect
     this part's own runtime test caught. A success handler that cleared the
     status would run on the microtask queue, AFTER a synchronous `error` from a
     clip that is not on disk had already put the reason on screen, and would
     wipe it: the reader would be shown silence with no explanation, which is the
     exact failure R6 is here to prevent. `begin` clears the status before it
     starts, so there is nothing left for a success to clear.

     ⛔ **A clip that is not on disk fires `error` AND rejects `play()`, in
     either order.** The rejection stands down when the passage it started is no
     longer playable, so an `error` that arrived first keeps its sentence; one that
     arrives second overwrites the blocked one on its own. */
  function start() {
    var started = audio.play();
    var index = at;
    if (started && started.catch) {
      started.catch(function () {
        if (index !== -1 && !playable[index]) { return; }
        /* ⚠️ A known state with a stated remedy, not an error: the browser
           refused to start audio without a gesture it recognised. */
        say(BLOCKED);
        showFace(PAUSED);
      });
    }
  }

  function begin(index, scroll) {
    if (index === -1 || !playable[index]) { return; }
    load(index);
    say(null);
    showFace(PLAYING);
    if (scroll) { reveal(passages[index]); }
    start();
  }

  function toggle() {
    if (!anyPlayable()) { return; }
    if (audio.paused) {
      if (at === -1) { at = firstPlayable(); }
      begin(at, false);
    } else {
      audio.pause();
      showFace(PAUSED);
    }
  }

  function step(direction, scroll) {
    if (!anyPlayable()) { return; }
    var target = at === -1 ? firstPlayable() : nextPlayable(at, direction);
    if (target === -1) { return; }
    begin(target, scroll);
  }

  /* ⛔ A clip the page names and the disk does not have. It stops here rather
     than skipping on: a cascade of silent skips is the failure that reports
     nothing, and this passage's own state is what the reader needs. */
  audio.addEventListener('error', function () {
    if (at === -1) { return; }
    playable[at] = false;
    showFace(PAUSED);
    say(anyPlayable() ? MISSING : NONE);
    if (!anyPlayable()) { disable(); }
  });

  audio.addEventListener('ended', function () {
    var target = nextPlayable(at, 1);
    if (target === -1) {
      showFace(PAUSED);
      at = passages.length - 1;
      progress();
      return;
    }
    begin(target, true);
  });

  audio.addEventListener('timeupdate', progress);
  audio.addEventListener('play', function () { showFace(PLAYING); });
  audio.addEventListener('pause', function () { showFace(PAUSED); });

  if (play) { play.addEventListener('click', toggle); }
  if (next) { next.addEventListener('click', function () { step(1, true); }); }
  if (previous) { previous.addEventListener('click', function () { step(-1, true); }); }
  if (speed) {
    speed.addEventListener('change', function () { audio.playbackRate = rate(); });
  }

  /* Click any passage to read from there — the template says so, so it holds.
     ⚠️ A click on a link or a button inside a passage is that control's, never
     the narrator's. */
  passages.forEach(function (passage, index) {
    passage.addEventListener('click', function (event) {
      if (!playable[index]) { return; }
      var target = event.target;
      if (target && target.closest && target.closest('a, button, select, summary')) { return; }
      begin(index, false);
    });
  });

  /* ⛔ The keyboard contract is the template's own sentence and this implements
     exactly it. ⚠️ Space is left alone on a focused control, where it activates
     that control, and in a field, where it is a space — hijacking either is how a
     page-wide shortcut becomes a bug nobody can type around. */
  var TYPING = { INPUT: true, TEXTAREA: true, SELECT: true, BUTTON: true, OPTION: true };

  document.addEventListener('keydown', function (event) {
    if (event.defaultPrevented || event.altKey || event.ctrlKey || event.metaKey) { return; }
    var target = event.target;
    if (target && (TYPING[target.tagName] || target.isContentEditable)) { return; }
    if (event.key === ' ' || event.key === 'Spacebar') {
      event.preventDefault();
      toggle();
    } else if (event.key === 'ArrowRight') {
      event.preventDefault();
      step(1, true);
    } else if (event.key === 'ArrowLeft') {
      event.preventDefault();
      step(-1, true);
    }
  });

  function disable() {
    [previous, play, next, speed].forEach(function (control) {
      if (control) { control.disabled = true; }
    });
  }

  /* ⛔ The transport is unhidden only once there is something behind it, and the
     unit with nothing to play says so with its controls off rather than showing
     three buttons that do nothing. */
  showFace(PAUSED);
  if (anyPlayable()) {
    /* ⛔ The first passage is where narration WILL start, and the
       transport's own line says so; nothing on the page is lit until the
       reader starts it. `load` lights a passage, and only a press reaches it. */
    at = firstPlayable();
    label();
    progress();
    say(null);
  } else {
    at = -1;
    label();
    progress();
    say(NONE);
    disable();
  }
  player.hidden = false;
}());

/* The practice panel: Run, Submit, the result, and what the Submit reported.

   ⛔ **This file draws; it never talks to the API.** Everything it sends goes
   through `window.studyforge.run` — `available()`, `start(corpus, practice,
   mode, onLine)`, `stop()` — which the SERVING PROCESS adds to the page it
   answers (`serve.routes.assets` says how). ⭐ That is the whole
   reason this part can live in a built site at all: a built text that named the
   API, the serving origin or the client file is a defect R8's floor reads
   (`tests/studyforge/cli/serving.py`), and there is no such name below.

   ⛔ **Over `file://` there is no origin to ask, so the controls stay HIDDEN**
   and the panel shows the sentence that says why. ⚠️ Nothing is disabled: a
   dead button is a promise the page cannot keep, which is this panel's own
   rule about the editor and about Submit.

   ⛔ **The practice key and the two mode words are read off the markup,
   verbatim.** `data-practice` carries the string `progress.practice_key` minted
   in Python and `data-practice-act` carries one of `exercise.COMMANDS`. Nothing
   here composes a key, splits one, encodes one or invents a mode — the client
   refuses a malformed key before any request, and a key spelled twice would
   simply never match anything with nothing failing anywhere.

   ⛔ **Nothing is written to browser storage.** A run's outcome is the SERVER's
   record (spec §8.5), written where it was established; a page that also
   remembered would be a second answer to *did this pass?*. ⭐ So nothing here
   has to be namespaced against the one storage origin every `file://` page
   shares.

   ## ⭐ THE TWO EDITOR WINDOWS ARE `practice-editor.js`'s

   ⚠️ **This file stood at `399` of R11's `400`** and the breakdown below had to
   go somewhere. ⛔ **Neither a size exception nor a trim of four other rows'
   prose was an honest answer**, so the split was taken
   at the seam by subject: the frames, their tablist and the one reload a cold
   instance needs are *the editor*, and this file is *the controls, the run and
   what the run reported*. ⭐ The two share nothing but the markup.

   ## ⛔ THE BREAKDOWN IS READ OFF THIS RUN'S OWN STREAM, NOT FETCHED

   ⛔ **A built page may name no API and no origin** (R8), so there is no
   asking the state namespace where the recorded breakdown lives. ⭐ **The run's
   response body is the one thing the server already hands this page**, and the
   verdicts are said on it — one framed `--- case <id>: passed|failed ---` per
   declared case, just before the exit line, by `serve.routes.breakdown`.

   ⭐ **The counts are DERIVED here and were never recorded**: Python renders
   every declared case with its id, its kind and the corpus's own sentence, and
   this joins the run's verdicts onto them. ⛔ **A population that does not match
   is shown as NOTHING rather than as a partial count** — a breakdown whose case
   set differs from the panel's is a breakdown of a different practice, which is
   what a corpus regenerated under a reader looks like.

   ⛔ **A reader shown *edge cases 2/3* is looking at an INCOMPLETE practice and
   never at a new kind of verdict.** `progress.is_pass` is untouched, this file
   never decides what passed, and the status line beside the breakdown is still
   the run's own word. */

(function () {
  'use strict';

  /* The panel, and the attributes it carries. ⚠️ Spelled here and in
     `render/page/practice.py`, which is the same two-sided spelling every hook
     on this page has: markup and script cannot import one another, and the
     Python side is the single source for what is EMITTED. */
  var PANEL = 'section[data-practice]';
  var KEY = 'data-practice';
  var CORPUS = 'data-corpus';
  var PART = 'data-practice-part';
  var ACT = 'data-practice-act';
  var STOP = 'stop';
  var TEST = 'test';

  /* The maximised panel's own mark, and where the control's OTHER word is kept
     — ⭐ both of its words are the template's, never this file's. */
  var EXPANDED = 'data-practice-expanded';
  var LABEL = 'data-practice-label';
  var ESCAPE = 'Escape';

  /* One declared case, its kind, and the mark a verdict leaves on it. ⚠️ The
     verdict is an attribute AND a word: a breakdown told apart only by colour
     is a breakdown a screen reader cannot read. */
  var CASE = 'data-practice-case';
  var CASE_KIND = 'data-practice-case-kind';
  var VERDICT = 'data-practice-verdict';
  var MAIN = 'main';
  var EDGE = 'edge';
  var PASSED = 'passed';
  var FAILED = 'failed';

  /* Every word the breakdown says, kept where the markup is. ⭐ The same reason
     the maximise control's second word lives in its template: a label
     spelled in the script too would be a second place for it to drift. */
  var SAYS = {
    done: 'data-practice-ask-done',
    missed: 'data-practice-ask-missed',
    edges: 'data-practice-edges',
    passed: 'data-practice-passed',
    failed: 'data-practice-failed'
  };

  /* What one case's verdict looks like on the stream. ⛔ Anchored whole, and the
     id must be one this panel DECLARES before anything is drawn — so a line of
     this shape out of a grader's own output reaches a case set that does not
     match and is shown as nothing. ⚠️ The record is unaffected either way: it is
     folded from the grader's report on the server, never from this text. */
  var CASE_LINE = /^--- case (.+): (passed|failed) ---$/;

  function part(panel, name) {
    return panel.querySelector('[' + PART + '="' + name + '"]');
  }

  function show(element, visible) {
    if (element) { element.hidden = !visible; }
  }

  function words(element, name) {
    return element.getAttribute(SAYS[name]) || '';
  }

  /* One line of output, appended as it arrives. ⚠️ `textContent`, never
     `innerHTML`: a program's own output is not markup, and a grader that
     printed a tag would otherwise be parsed as one. */
  function append(output, line) {
    output.appendChild(document.createTextNode(line + '\n'));
    output.scrollTop = output.scrollHeight;
  }

  /* What a finished run is called, from the verdict the client resolves with:
     a status number, 'timeout' or 'stopped'. ⛔ The server decides what a run
     MEANT and records it; this only says what the reader just watched. */
  function verdict(answer, mode) {
    if (answer === 'stopped') { return 'Stopped.'; }
    if (answer === 'timeout') { return 'Timed out.'; }
    if (answer !== 0) { return 'Finished with errors.'; }
    return mode === 'run' ? 'Finished.' : 'Passed.';
  }

  function refusal(answer) {
    if (answer && answer.refused === 409) {
      return 'Something is already running. Stop it first.';
    }
    return 'That could not be started.';
  }

  /* ⭐ **MAXIMISE: the PANEL'S OWN GEOMETRY, never a reparent** — the
     panel already holds all of it, so the move is one attribute on the section.

     ⛔ **A frame is never moved to another parent.** An `iframe` REPARENTED IN
     THE DOM RELOADS, so nothing below appends, removes or replaces a node.

     ⛔ **THE SCROLL POSITION IS REMEMBERED AND PUT BACK INSTANTLY** (argued
     where the rule is, in `practice.css`).

     ⛔ **No keyboard exit would make this a trap.** A real button, focus into
     the expanded practice and back on restore, Escape on the DOCUMENT (focus
     may rest on `<body>`, and inside the editor frame Escape is the editor's). */
  function maximise(panel) {
    var button = part(panel, 'expand');
    if (!button) { return; }
    var both = [button.textContent, button.getAttribute(LABEL) || button.textContent];
    var wide = false;
    var was = 0;

    function set(open) {
      if (open) { was = window.pageYOffset || 0; }
      wide = open;
      if (open) { panel.setAttribute(EXPANDED, ''); } else { panel.removeAttribute(EXPANDED); }
      button.setAttribute('aria-expanded', open ? 'true' : 'false');
      button.textContent = both[open ? 1 : 0];
      (open ? panel : button).focus({ preventScroll: true });
      /* ⛔ **`'instant'` is the repair, not a flourish**: `reset.css` sets
         `scroll-behavior: smooth`, so a plain `scrollTo` ANIMATES and the page
         is still gliding when whatever looks at it next does, anywhere from a
         couple of pixels to the panel's whole height away. */
      if (!open) { window.scrollTo({ top: was, left: 0, behavior: 'instant' }); }
    }

    show(button, true);
    button.addEventListener('click', function () { set(!wide); });
    document.addEventListener('keydown', function (event) {
      if (wide && event.key === ESCAPE) { set(false); }
    });
  }

  /* ⭐ **Every declared case, marked with what THIS run said about it.** ⛔ Drawn
     only when the two populations are the same set, and cleared to nothing
     whenever they are not — the argument is at the top of this file. */
  function breakdown(panel) {
    var region = part(panel, 'breakdown');
    if (!region) { return null; }
    var rows = [].slice.call(region.querySelectorAll('[' + CASE + ']'));
    var summary = part(region, 'summary');

    function clear() {
      show(region, false);
      rows.forEach(function (row) {
        row.removeAttribute(VERDICT);
        var mark = part(row, 'verdict');
        if (mark) { mark.textContent = ''; }
      });
      if (summary) { summary.textContent = ''; }
    }

    /* ⛔ Both directions, because either alone lets a partial count through: a
       case the run never named, and a name this panel never declared. */
    function whole(said) {
      var known = 0;
      rows.forEach(function (row) {
        if (Object.prototype.hasOwnProperty.call(said, row.getAttribute(CASE))) { known += 1; }
      });
      return !!rows.length && known === rows.length && known === Object.keys(said).length;
    }

    function draw(said) {
      if (!whole(said)) { clear(); return; }
      var edges = 0;
      var passed = 0;
      var ask = true;
      rows.forEach(function (row) {
        var right = said[row.getAttribute(CASE)];
        var edge = row.getAttribute(CASE_KIND) === EDGE;
        var mark = part(row, 'verdict');
        row.setAttribute(VERDICT, right ? PASSED : FAILED);
        if (mark) { mark.textContent = words(region, right ? 'passed' : 'failed'); }
        if (edge) { edges += 1; }
        if (edge && right) { passed += 1; }
        if (row.getAttribute(CASE_KIND) === MAIN && !right) { ask = false; }
      });
      if (summary) {
        summary.textContent = words(region, ask ? 'done' : 'missed') + ' ' +
          words(region, 'edges').replace('{passed}', passed).replace('{total}', edges);
      }
      show(region, true);
    }

    return { clear: clear, draw: draw };
  }

  function wire(panel, run) {
    var key = panel.getAttribute(KEY);
    var corpus = panel.getAttribute(CORPUS);
    var controls = part(panel, 'controls');
    var status = part(panel, 'status');
    var output = part(panel, 'output');
    var acts = [].slice.call(panel.querySelectorAll('[' + ACT + ']'));
    if (!key || !corpus || !controls || !status || !output || !acts.length) { return; }
    var cases = breakdown(panel);

    /* ⭐ The editor slot is shown, and what it shows is the sentence saying the
       editor is not running. Hiding it instead would be the blank panel this
       row exists to refuse. ⚠️ What FILLS it is `practice-editor.js`'s. */
    show(part(panel, 'offline'), false);
    show(part(panel, 'editor'), true);
    show(controls, true);
    /* ⚠️ Offered where there is something to maximise: over `file://` the panel
       is one sentence, and making a sentence full-screen is a dead button. */
    maximise(panel);

    var stop = null;
    var starters = [];
    acts.forEach(function (button) {
      if (button.getAttribute(ACT) === STOP) { stop = button; } else { starters.push(button); }
    });

    /* ⛔ **Focus follows the control that goes away, and that is keyboard
       correctness rather than polish.** A button that is disabled or hidden
       while it holds focus drops focus to the document, and a keyboard reader
       is returned to the top of the page mid-run. So the start moves focus to
       Stop and the end gives it back to the button that was pressed — and only
       ever when this panel already had it. */
    var pressed = null;

    /* ⛔ **Set when STOP takes focus away from this panel itself**, which is the
       one hand-back `holdsFocus()` cannot answer for. Pressing Stop disables
       Stop, a disabled element drops focus to the document AT ONCE, and the
       run then settles a moment later with focus already on `<body>` — so the
       question *did the panel have focus?* answers no and the keyboard reader
       is left at the top of the page. ⚠️ The ordinary end-of-run path does
       not have this problem; only Stop does. */
    var handedBack = false;

    /* ⚠️ Asked BEFORE the control is disabled or hidden, never after: a
       disabled element drops focus to the document immediately, so a check
       taken afterwards always answers no and the reader is left at the top of
       the page. */
    function holdsFocus() {
      return panel.contains(document.activeElement);
    }

    function live(running) {
      starters.forEach(function (button) { button.disabled = running; });
      show(stop, running);
    }

    function settle(text, said) {
      var keyboard = holdsFocus() || handedBack;
      handedBack = false;
      status.textContent = text;
      if (cases && said) { cases.draw(said); }
      live(false);
      /* ⛔ AFTER `live(false)`: the button that was pressed is disabled while
         the run is live, and focusing a disabled control does nothing at all. */
      if (keyboard && pressed) { pressed.focus(); }
    }

    starters.forEach(function (button) {
      button.addEventListener('click', function () {
        var mode = button.getAttribute(ACT);
        var keyboard = holdsFocus();
        var said = mode === TEST ? {} : null;
        pressed = button;
        output.textContent = '';
        show(output, true);
        status.textContent = 'Running…';
        if (cases) { cases.clear(); }
        live(true);
        if (keyboard && stop) { stop.focus(); }
        run.start(corpus, key, mode, function (line) {
          /* ⛔ A case line is the SERVER talking about this run rather than the
             program's own output, so it is taken OFF the stream instead of
             standing raw beside the breakdown it feeds — the same way the client
             already takes the exit line. ⚠️ Every other line, the breakdown's
             own refusal included, reaches the reader unchanged. */
          var found = said ? CASE_LINE.exec(line) : null;
          if (found) { said[found[1]] = found[2] === PASSED; } else { append(output, line); }
        }).then(
          function (answer) { settle(verdict(answer, mode), said); },
          function (answer) { settle(refusal(answer), null); }
        );
      });
    });

    if (stop) {
      stop.addEventListener('click', function () {
        /* ⛔ Asked BEFORE the line below, for the reason `holdsFocus` states:
           this IS the disable that drops focus to the document. */
        handedBack = holdsFocus();
        stop.disabled = true;
        run.stop().then(
          function () { stop.disabled = false; },
          function () { stop.disabled = false; }
        );
      });
    }
  }

  var panels = [].slice.call(document.querySelectorAll(PANEL));
  if (!panels.length) { return; }
  var run = window.studyforge && window.studyforge.run;
  if (!run || !run.available()) { return; }
  panels.forEach(function (panel) { wire(panel, run); });
}());

/* The practice panel's two editor windows: where each one is, and the tablist over them.

   ⛔ **Split out of `practice.js` at its seam**, and the split is
   its own act rather than a passenger: that file stood at `399` of
   R11's `400` and the Submit breakdown had behaviour to add to the panel. ⭐ The seam is the
   SUBJECT — `practice.js` is *the controls, the run and what the run reported*,
   and this is *the two windows of the editor* — and the two share nothing but
   the markup, which is why neither has to reach into the other.

   ⛔ **This file draws; it never talks to the API.** Everything it asks goes
   through `window.studyforge.run.practice(corpus, key)`, which the SERVING
   PROCESS adds to the page it answers. ⭐ A built text that named the
   API, the serving origin or the client file is a defect R8's floor reads
   (`tests/studyforge/cli/serving.py`), and there is no such name below.

   ⛔ **The editor is NOT started from here, and that is deliberate.** It is a
   development environment with a shell, and opening a reading page is not
   consent to run one. The slot carries the sentence saying it is not running
   and how to start it, so a reader sees a statement rather than a blank frame —
   ⭐ and when the server answers where this practice's two windows are, the
   frames replace that sentence. ⛔ **Every URL is the SERVER's
   answer, never a name in this file**: a built page may name no origin and no
   port (R8), the editor's host port is per-project, and the absolute path a
   window opens is a path inside somebody else's container.

   ⛔ **This panel does not make anything read-only and never says it is.** The
   editor enforces that itself, out of the workspace settings the server writes
   — a guard here would be a second, weaker copy of a rule the editor keeps.

   ## ⛔ ONE reload, and only a genuinely COLD instance can ever need it

   ⭐ **What a served page may frame is composed from the editor origins the
   SERVING INSTANCE has discovered**, and a cold instance has discovered none
   until a reader's own client asks — which this panel does, exactly one
   document too late: the policy governing THIS document was sent before the
   ask. ⛔ **The serving process may not ask earlier.** Discovering an editor
   forks `docker`, and putting that on the path of an ordinary page response is
   refused outright (spec §8.3), so the remaining move is the client's.

   ⭐ **The browser is ASKED rather than guessed at.** A
   `securitypolicyviolation` naming `frame-src` and this editor's own host is
   the browser stating that the frame was blocked; the reload then gets a
   document composed from the record that ask has just filled. ⛔ **Nothing is
   reloaded on a hunch** — no violation, no reload.

   ⛔ **Three guards, and each one closes a real loop.** The blocked URI must be
   the editor's host, so an unrelated violation reloads nothing. The editor's
   host must be the host THIS page was reached at, because a server withholds an
   editor from another spelling of the same machine on purpose and no number of
   reloads would change that. And this navigation must not itself be a reload,
   which caps the whole remedy at one. ⚠️ **A browser with no navigation timing
   is not reloaded at all**: failing closed is a frame that does not load, and
   failing open is a page that reloads for ever. */

(function () {
  'use strict';

  /* The panel and its parts. ⚠️ Spelled here, in `practice.js` and in
     `render/page/practice.py` — the same two-sided spelling every hook on this
     page has: markup and script cannot import one another, and the Python side
     is the single source for what is EMITTED. */
  var PANEL = 'section[data-practice]';
  var KEY = 'data-practice';
  var CORPUS = 'data-corpus';
  var PART = 'data-practice-part';
  var TAB = 'data-practice-tab';
  var FRAME = 'data-practice-frame';

  /* The two windows, and what each frame is called to a screen reader. ⚠️ These
     are the framework's own words for its own controls, not the material's
     (R1) — the same status the panel's 'Running…' and 'Passed.' already have. */
  var WINDOWS = ['main', 'test'];
  var TITLES = { main: 'Your code', test: 'Tests' };

  /* The directive a blocked editor frame is refused by, in the browser's own
     spelling. ⚠️ Compared as a PREFIX rather than for equality, because the
     older `violatedDirective` reports the whole directive — `frame-src 'none'`
     — where `effectiveDirective` reports only its name. */
  var FRAME_SRC = 'frame-src';

  function part(panel, name) {
    return panel.querySelector('[' + PART + '="' + name + '"]');
  }

  function show(element, visible) {
    if (element) { element.hidden = !visible; }
  }

  /* ⛔ **A FRAME NEVER TAKES FOCUS THE READER DID NOT GIVE IT, AND THE PAGE
     NEVER MOVES ON ITS OWN**.

     ⚠️ **The mechanism, as a real browser behaves.** A
     workbench focuses its editor as it starts — `restoreParts()` calls
     `activeGroup.focus()`, then the editor that opens the window's file calls
     `focus()` on its input, neither with `preventScroll` — and the browser lets
     a frame of another origin on the same site take focus from the page with
     no user activation at all. ⛔ **Focusing an element scrolls every ancestor
     frame to it**, so a reader who opened the page at its top was carried to
     the editor seconds later, and `document.activeElement` would become the frame.
     ⚠️ Nothing on the frame refuses it: `inert` does not reach the framed
     document, and `allow="focus-without-user-activation 'none'"` is not
     honoured.

     ⭐ **So the page gives focus back, and it can because of an order the
     browser keeps.** The page's `blur` is dispatched INSIDE the frame's
     `focus()` call, before the scroll it starts has moved anything; one task
     later focus goes back to where the reader left it and the page is put back
     where it was, which also cancels the glide `scroll-behavior: smooth` had
     queued. ⛔ **The reader never sees the page move**: the page rests where
     it was opened, and at most one 3px step is painted before it is put back.

     ⭐ **What counts as GIVEN is the reader's hand, never a timer:** the
     pointer over THAT frame together with the page's own user activation —
     which a click inside a frame propagates to every ancestor — or a Tab
     pressed on the page just before focus arrived. ⚠️ **Two steals raise NO
     `blur`**: a frame taking focus while the reader types in ANOTHER frame, and
     any frame taking it while the browser window itself is not focused (the
     page still scrolls). So the page also reads `activeElement`
     every `WATCH_EVERY` ms for as long as it carries a frame, and answers
     those the same way. ⭐ Nothing is installed until the first frame is
     built, so a page with no editor carries none of it. */
  var held = (function () {
    var TAB_GRACE = 500;
    var WATCH_EVERY = 100;
    var frames = [];
    var pointed = null;
    var tabbed = -Infinity;
    var trusted = null;
    var previous = null;
    var resting = { x: 0, y: 0 };

    function ours(element) {
      return frames.indexOf(element) >= 0 ? element : null;
    }

    function here() {
      return { x: window.scrollX, y: window.scrollY };
    }

    function given(built) {
      var state = navigator.userActivation;
      var active = state ? state.isActive : true;
      return (pointed === built && active) || Date.now() - tabbed < TAB_GRACE;
    }

    /* ⚠️ Put the page back, and CANCEL the glide the frame queued. A scroll to
       where the page already is does nothing, and a glide not yet begun
       survives it (the page creeps 3px and stops there), so the
       page is moved one pixel and back — both instant, within one task, so no
       frame is ever painted between them. ⚠️ A glide already under way can
       still land one step after that, so the next two
       frames look again. */
    function stay(at, again) {
      if (window.scrollX !== at.x || window.scrollY !== at.y || again === undefined) {
        var nudge = at.y > 0 ? at.y - 1 : at.y + 1;
        window.scrollTo({ left: at.x, top: nudge, behavior: 'instant' });
        window.scrollTo({ left: at.x, top: at.y, behavior: 'instant' });
      }
      var left = again === undefined ? 2 : again;
      if (left > 0) { requestAnimationFrame(function () { stay(at, left - 1); }); }
    }

    /* ⚠️ One task later, and not inside the `blur`: a focus moved while the
       browser is still dispatching the frame's own focus change is ignored,
       and a MICROTASK is still inside it (`activeElement`
       stays the frame and the page glides to it). ⛔ Whichever frame holds
       focus by THEN is the one answered: the second practice's workbench can
       take it from the first in between, and raises no event here. */
    function refuse(at) {
      setTimeout(function () {
        var built = ours(document.activeElement);
        if (!built || built === trusted) { return; }
        var back = !!previous && previous !== built && previous !== document.body &&
          document.contains(previous);
        if (back) { previous.focus({ preventScroll: true }); } else { built.blur(); }
        stay(at);
      }, 0);
    }

    function arrived(at) {
      var built = ours(document.activeElement);
      if (!built || built === trusted) { return; }
      if (given(built)) {
        trusted = built;
        previous = built;
      } else {
        refuse(at);
      }
    }

    /* The only way to see the two steals that raise no `blur`. ⚠️ `resting` is
       where the page was one tick ago, which is before a steal it now sees. */
    function tick() {
      arrived(resting);
      resting = here();
    }

    function install() {
      setInterval(tick, WATCH_EVERY);
      document.addEventListener('focusin', function (event) { previous = event.target; }, true);
      /* Focus back on the page itself, which raises no `focusin` when it lands
         on the body: the page, not the frame it left, is where it now is. */
      window.addEventListener('focus', function () {
        trusted = null;
        previous = document.activeElement;
      });
      document.addEventListener('keydown', function (event) {
        if (event.key === 'Tab') { tabbed = Date.now(); }
      }, true);
      window.addEventListener('blur', function () {
        resting = here();
        arrived(resting);
      });
    }

    return function (built) {
      if (!frames.length) { install(); }
      frames.push(built);
      built.addEventListener('pointerenter', function () { pointed = built; });
      built.addEventListener('pointerleave', function () {
        if (pointed === built) { pointed = null; }
      });
    };
  }());

  function frame(slot, url, title) {
    var built = document.createElement('iframe');
    built.src = url;
    built.title = title;
    /* ⛔ BEFORE it is added: the workbench may take focus as soon as it loads. */
    held(built);
    slot.appendChild(built);
  }

  /* A URL's host, without its port and without its scheme. ⚠️ An IPv6 literal
     keeps its brackets, which is the spelling `location.hostname` uses too. */
  function hostOf(url) {
    var found = /^[a-z]+:\/\/([^/?#]*)/i.exec(String(url || ''));
    return found ? found[1].replace(/:\d+$/, '').toLowerCase() : '';
  }

  /* Reload once if, and only if, the browser says this document's policy
     blocked the editor's frame. ⛔ The three guards are argued at the top of
     this file, and each of them closes a loop rather than tidying one. */
  function reloadWhenBlocked(url) {
    var editor = hostOf(url);
    var timing = window.performance && window.performance.getEntriesByType
      ? window.performance.getEntriesByType('navigation')
      : [];
    if (!editor || editor !== String(location.hostname || '').toLowerCase()) { return; }
    if (!timing.length || timing[0].type === 'reload') { return; }
    var reloaded = false;
    document.addEventListener('securitypolicyviolation', function (event) {
      var directive = event.effectiveDirective || event.violatedDirective || '';
      if (reloaded || directive.indexOf(FRAME_SRC) !== 0) { return; }
      if (hostOf(event.blockedURI) !== editor) { return; }
      reloaded = true;
      location.reload();
    });
  }

  /* ⭐ **Two frames of ONE editor, one visible at a time — never a split pane.**
     The file a reader may type in and the file that judges it are two different
     acts of reading, and standing them side by side halves the width of both.

     ⛔ **Each frame's URL is the SERVER's answer and is never built here**: the
     window's own URL is the only thing that can point two windows of one editor
     at two different files, because an extension cannot read its own window's
     query string and both windows share one workspace settings file.

     ⛔ **The TESTS frame is built LAZILY, on the first click of its tab.** A
     second workbench is a second language server, and a reader who never opens
     the tests should never pay for one. */
  function windows(panel, where) {
    var slots = {};
    WINDOWS.forEach(function (name) {
      slots[name] = panel.querySelector('[' + FRAME + '="' + name + '"]');
    });
    if (!slots.main) { return; }
    var tested = !!(where.test && where.test.url);
    var buttons = [].slice.call(panel.querySelectorAll('[' + TAB + ']')).filter(
      function (button) {
        var keep = tested || button.getAttribute(TAB) !== 'test';
        if (!keep) { button.hidden = true; }
        return keep;
      }
    );
    var lazy = false;

    function select(name) {
      buttons.forEach(function (button) {
        var mine = button.getAttribute(TAB) === name;
        button.setAttribute('aria-selected', mine ? 'true' : 'false');
        button.tabIndex = mine ? 0 : -1;
      });
      WINDOWS.forEach(function (one) { show(slots[one], one === name && !!slots[one]); });
      if (name === 'test' && !lazy && tested) {
        lazy = true;
        frame(slots.test, where.test.url, TITLES.test);
      }
    }

    /* ⛔ BEFORE the frame is added, because the violation it listens for is
       raised by adding it. */
    reloadWhenBlocked(where.main.url);
    frame(slots.main, where.main.url, TITLES.main);
    buttons.forEach(function (button) {
      button.addEventListener('click', function () { select(button.getAttribute(TAB)); });
      /* ⚠️ Arrow keys move between tabs, which is what a tablist is announced
         as promising. Without them the roles say one thing and the keyboard
         does another. */
      button.addEventListener('keydown', function (event) {
        var step = event.key === 'ArrowRight' ? 1 : event.key === 'ArrowLeft' ? -1 : 0;
        var next = buttons.indexOf(button) + step;
        if (!step || next < 0 || next >= buttons.length) { return; }
        event.preventDefault();
        buttons[next].focus();
        select(buttons[next].getAttribute(TAB));
      });
    });
    select('main');
    /* ⭐ One tab is no choice, so the tablist stays hidden where the material
       names no test — the same honesty as offering no Submit. */
    show(part(panel, 'tabs'), tested);
    show(part(panel, 'no-editor'), false);
  }

  /* ⭐ Fill the editor slot when the server says where THIS PRACTICE's two
     windows are, and leave the sentence standing when it does not. ⛔ Frames are
     added only for an editor that is already up over this corpus's own files and
     that actually holds this practice's file — the server decides both, this
     asks.

     ⚠️ **Asked for, never assumed.** A site BUILT by one version of this
     framework may be SERVED by another, and the client is the serving process's;
     a panel that called a function an older client does not publish would take
     the whole editor slot down with it. */
  function ask(panel, run) {
    var key = panel.getAttribute(KEY);
    var corpus = panel.getAttribute(CORPUS);
    if (!key || !corpus || !run.practice) { return; }
    run.practice(corpus, key).then(function (where) {
      if (where && where.main && where.main.url) { windows(panel, where); }
    }, function () { return null; });
  }

  /* ⭐ The frame and its one reload are PUBLISHED, so a lesson's code panel
     (`code-links.js`) builds its windows with the same focus guard and the
     same remedy for a cold instance, rather than a second copy of either. */
  window.studyforge = window.studyforge || {};
  window.studyforge.frames = { frame: frame, reloadWhenBlocked: reloadWhenBlocked };

  var panels = [].slice.call(document.querySelectorAll(PANEL));
  if (!panels.length) { return; }
  var run = window.studyforge && window.studyforge.run;
  if (!run || !run.available()) { return; }
  panels.forEach(function (panel) { ask(panel, run); });
}());

/* The quiz: what a reader chose, and what the local study server said about it.

   ⛔ **THE KEY IS NOT IN THE PAGE, AND THIS FILE DOES NOT GRADE**: the correct
   answers stay on the local server, which validates each answer and returns
   the result with its explanation. ⭐ So this file reads
   which option the reader chose, hands the choices to `window.studyforge.quiz`
   — which the SERVING PROCESS adds to a served page and a built page never
   names (R8) — and shows what came back: right or wrong per question,
   the chosen option's sentence, the count, and whether the quiz is complete.
   ⛔ **The completion rule is the server's** (`exercise.quiz.completes`, applied
   once, in Python); this file shows `complete` and never re-derives it.

   ⚠️ **Superseded, and kept readable so it is not re-derived:** until that
   ruling this file graded in the page from a key every option carried, identically
   over `file://`, on the stance that an offline page cannot hide the key it grades
   with. The ruling removes the key from the page instead.

   ⭐ **Over `file://` the questions and the options still show** and a reader
   may still choose; the Check control stays `hidden` and the `offline`
   sentence stays showing, exactly as Run and Submit do in the code panel. ⛔
   Nothing is sent from a file page — there is no origin to send it to.

   ⛔ **A quiz has no file, no command and no grader to submit to, so it renders
   no Run and no Submit — and not disabled ones**: a dead button is never
   rendered.

   ⛔ **Nothing is written to browser storage, and the server records nothing
   either.** What a reader answered is the page's for as long as they are on
   it; ⚠️ **so a reload clears the answers**, and recording a quiz's completion
   in the reader's own state is not done here — and it would never be a
   run verdict, which a quiz does not produce.

   ⭐ **Every word this file says is read off the markup**, where Python put it —
   the same two-sided spelling every hook on this page has. */

(function () {
  'use strict';

  var QUIZ = 'section[data-practice-quiz]';
  var PART = 'data-practice-part';
  var QUESTION = 'data-practice-question';
  var VERDICT = 'data-practice-verdict';

  /* Where each of this file's own sentences is kept. ⚠️ `right` is read off two
     different elements and means two different things — the question's *Right.*
     and the section's counting line — which is why it is asked for by element
     rather than looked up in one table. */
  var RIGHT = 'data-practice-right';
  var WRONG = 'data-practice-wrong';
  var COMPLETE = 'data-practice-complete';
  var BLANK = 'data-practice-blank';
  var CHECKING = 'data-practice-checking';
  var FAILED = 'data-practice-failed';

  function part(root, name) {
    return root.querySelector('[' + PART + '="' + name + '"]');
  }

  function words(element, name) {
    return (element && element.getAttribute(name)) || '';
  }

  /* What the reader chose, as `{question id: option id}`. ⚠️ Read off the DOM
     rather than remembered: the radios ARE the state, and a second copy of them
     would be a second answer to *what did they choose?*. */
  function chosen(quiz) {
    var answers = {};
    [].slice.call(quiz.querySelectorAll('[' + QUESTION + ']')).forEach(function (question) {
      var picked = question.querySelector('input[type="radio"]:checked');
      if (picked) { answers[question.getAttribute(QUESTION)] = picked.value; }
    });
    return answers;
  }

  /* One question's row of the server's verdict, drawn. ⭐ The sentence is the
     one for whatever the reader CHOSE, right or wrong — a page that showed one
     only for a wrong answer would teach half the material. ⛔ A question the
     verdict says nobody answered shows nothing. */
  function draw(question, row) {
    var says = part(question, 'says');
    if (!row || !row.answered) {
      question.removeAttribute(VERDICT);
      if (says) { says.textContent = ''; says.hidden = true; }
      return;
    }
    question.setAttribute(VERDICT, row.correct ? 'correct' : 'wrong');
    if (says) {
      says.textContent = words(says, row.correct ? RIGHT : WRONG) + ' ' + (row.says || '');
      says.hidden = false;
    }
  }

  function show(quiz, verdict) {
    var rows = {};
    (verdict.questions || []).forEach(function (row) { rows[row.id] = row; });
    [].slice.call(quiz.querySelectorAll('[' + QUESTION + ']')).forEach(function (question) {
      draw(question, rows[question.getAttribute(QUESTION)]);
    });
    var status = part(quiz, 'status');
    if (!status) { return; }
    /* ⛔ **Complete is the SERVER's word** and is EVERY question answered
       correctly; the count is said in every other case so a reader is never
       told only that they are not finished. */
    status.textContent = verdict.complete === true
      ? words(status, COMPLETE)
      : words(status, RIGHT).replace('{right}', verdict.right).replace('{asked}', verdict.asked);
  }

  function wire(quiz, client) {
    var check = part(quiz, 'check');
    var controls = part(quiz, 'controls');
    var offline = part(quiz, 'offline');
    var status = part(quiz, 'status');
    if (!check || !controls) { return; }
    if (offline) { offline.hidden = true; }
    controls.hidden = false;
    var graded = false;
    /* ⭐ Only the LATEST request may draw: a reader who changes an answer while
       the previous one is still being checked must never see the older verdict
       land on top of the newer choice. */
    var asked = 0;

    function grade() {
      var answers = chosen(quiz);
      var ticket = ++asked;
      if (!Object.keys(answers).length) {
        show(quiz, { questions: [], right: 0, asked: 0, complete: false });
        if (status) { status.textContent = words(status, BLANK); }
        return;
      }
      if (status) { status.textContent = words(status, CHECKING); }
      client.grade(quiz.getAttribute('data-corpus'), quiz.getAttribute('data-practice-quiz'), answers)
        .then(function (verdict) {
          if (ticket === asked) { show(quiz, verdict); }
        }, function () {
          if (ticket === asked && status) { status.textContent = words(status, FAILED); }
        });
    }

    /* ⭐ Re-graded as soon as a reader changes an answer, once they have asked
       once, so the sentence under a question can never describe an option that
       is no longer chosen. */
    check.addEventListener('click', function () { graded = true; grade(); });
    quiz.addEventListener('change', function () { if (graded) { grade(); } });
  }

  var client = window.studyforge && window.studyforge.quiz;
  if (!client || !client.available()) { return; }
  [].slice.call(document.querySelectorAll(QUIZ)).forEach(function (quiz) { wire(quiz, client); });
}());

/* A lesson's links to its own code: opened in the course's editor, beside the test.

   ⭐ **A link the build marked** (`data-code-path`, `render/page/code.py`)
   names a code file of the corpus, and its href is the file's plain view
   (`unit.mentions`). Served, with the corpus's editor up, a click opens the file in the
   page's code panel instead: the source and its test in two windows of ONE
   editor, and a Run that runs the test. ⛔ **Anything short of that follows the
   link**: no server, no editor, a file the server will not open — the plain
   view is never broken, and the panel's built sentence says why.

   ⛔ **This file draws; it never talks to the API.** Everything it asks goes
   through `window.studyforge.run` — `editor`, `code`, `codeTest`, `stop` —
   which the SERVING PROCESS adds to the page it answers, so a built text names
   no API, no origin and no client file (R8).

   ⭐ **The same frames as a practice.** Each window is built by
   `window.studyforge.frames.frame`, which `practice-editor.js` publishes — the
   same focus guard, so a workbench never takes the reader's focus or moves
   the page — and the same one reload when a cold instance's frame policy
   blocked the frame. ⛔ Neither is copied here.

   ⭐ **The copy is said, not hidden**: the panel's second sentence, shown only
   once the editor answered, tells the reader the editor opens a COPY of the
   code, where their changes and a run's output stay. */

(function () {
  'use strict';

  /* ⚠️ Spelled here and in `render/page/code.py` and `code-panel.html` — the
     two-sided spelling every hook on this page has. The panel reuses the
     practice panel's part names so `practice.css` draws both. */
  var REGION = 'section[data-code]';
  var CORPUS = 'data-corpus';
  var PATH = 'data-code-path';
  var PART = 'data-practice-part';
  var TAB = 'data-practice-tab';
  var FRAME = 'data-practice-frame';
  var ACT = 'data-code-act';
  var WINDOWS = ['main', 'test'];
  var TITLES = { main: 'Source', test: 'Test' };

  var region = document.querySelector(REGION);
  if (!region) { return; }
  var run = window.studyforge && window.studyforge.run;
  var frames = window.studyforge && window.studyforge.frames;
  if (!run || !run.available() || !run.code || !run.editor || !frames) { return; }
  var corpus = region.getAttribute(CORPUS);

  function part(name) { return region.querySelector('[' + PART + '="' + name + '"]'); }
  function show(element, visible) { if (element) { element.hidden = !visible; } }

  var buttons = [].slice.call(region.querySelectorAll('[' + TAB + ']'));
  var slots = {};
  WINDOWS.forEach(function (name) {
    slots[name] = region.querySelector('[' + FRAME + '="' + name + '"]');
  });
  var act = region.querySelector('[' + ACT + '="test"]');
  var stop = region.querySelector('[' + ACT + '="stop"]');
  var status = part('status');
  var output = part('output');
  var current = null;
  var built = {};

  function select(name) {
    buttons.forEach(function (button) {
      var mine = button.getAttribute(TAB) === name;
      button.setAttribute('aria-selected', mine ? 'true' : 'false');
      button.tabIndex = mine ? 0 : -1;
    });
    WINDOWS.forEach(function (one) { show(slots[one], one === name); });
    if (!built[name] && current[name] && current[name].url) {
      built[name] = true;
      frames.frame(slots[name], current[name].url, TITLES[name]);
    }
  }

  /* ⭐ One pair at a time: a new click empties both windows and opens the new
     pair, the file clicked in front. */
  function draw(where) {
    current = where;
    built = {};
    WINDOWS.forEach(function (name) { slots[name].textContent = ''; });
    var both = !!(where.test && where.test.url);
    buttons.forEach(function (button) { show(button, !!where[button.getAttribute(TAB)]); });
    show(part('tabs'), both);
    frames.reloadWhenBlocked(where.main.url);
    show(part('editor'), true);
    show(part('controls'), !!where.runs);
    status.textContent = '';
    show(output, false);
    select(where.opened === 'test' && both ? 'test' : 'main');
    setTimeout(function () { remember(null); }, 5000);
    region.focus({ preventScroll: true });
    region.scrollIntoView({ block: 'start' });
  }

  /* ⚠️ A COLD instance's page is reloaded once, by `frames.reloadWhenBlocked`,
     when its frame policy blocked the first frame — and a reload forgets the
     click. ⭐ So the file asked for rides on this history entry's own state,
     which a reload keeps and nothing else reads, until it is drawn; a page that
     was just reloaded opens it again. ⛔ Not the browser's store: that is
     `study-progress.js`'s alone. ⛔ Only a reload reopens it, and a history
     that cannot carry it is no history: the reader clicks again. */
  var REOPEN = 'studyforgeCode';

  function remember(path) {
    try {
      var state = {};
      state[REOPEN] = path || null;
      history.replaceState(state, '');
    } catch (ignored) { return; }
  }

  function reopened() {
    try {
      var timing = performance.getEntriesByType('navigation');
      var path = history.state && history.state[REOPEN];
      remember(null);
      return timing.length && timing[0].type === 'reload' ? path : null;
    } catch (ignored) { return null; }
  }

  function open(path, href) {
    var fallback = function () { remember(null); if (href) { location.href = href; } };
    remember(path);
    run.code(corpus, path).then(function (where) {
      if (where) { draw(where); } else { fallback(); }
    }, fallback);
  }

  buttons.forEach(function (button) {
    button.addEventListener('click', function () {
      if (current) { select(button.getAttribute(TAB)); }
    });
  });

  act.addEventListener('click', function () {
    if (!current || !current.runs) { return; }
    act.disabled = true;
    show(stop, true);
    output.textContent = '';
    show(output, true);
    status.textContent = 'Running the test…';
    run.codeTest(corpus, current.runs, function (line) {
      output.textContent += line + '\n';
    }).then(function (verdict) {
      status.textContent = verdict === 0 ? 'Passed.' : verdict === 'stopped' ? 'Stopped.'
        : verdict === 'timeout' ? 'Timed out.' : 'Failed.';
    }, function () {
      status.textContent = 'The test could not be run.';
    }).then(function () {
      act.disabled = false;
      show(stop, false);
    });
  });

  stop.addEventListener('click', function () { run.stop(); });

  /* ⭐ Only an editor that is UP turns the links into the panel; until then
     every link is the plain view and the built sentence stands. */
  run.editor(corpus).then(function (found) {
    if (!found) { return; }
    show(part('plain'), false);
    show(part('copy'), true);
    document.addEventListener('click', function (event) {
      if (event.defaultPrevented || event.button !== 0) { return; }
      if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) { return; }
      var anchor = event.target.closest ? event.target.closest('a[' + PATH + ']') : null;
      if (!anchor) { return; }
      event.preventDefault();
      open(anchor.getAttribute(PATH), anchor.href);
    });
    var again = reopened();
    if (again) { open(again, null); }
  }, function () { return null; });
}());

/* Where the reader is: the Up next slip, the progress line and strip, the tick
   of the unit up next, the filter and the two expand controls, and the rail's
   fold on a narrow screen.

   ⛔ **Every word a reader sees is markup.** The index and container renderers
   write each sentence with its numbers at zero and its alternatives hidden;
   this file only writes numbers into `<b>`/`<strong>`, moves an href, and
   hides or shows what is already there. Nothing here composes a sentence.

   ⛔ **Joined by the unit key and nothing else**, as `read-mark.js` is: a row
   on the index or a container page carries its key as its `id`, a rail row
   carries it as `data-unit`, and the store holds keys. Nothing here
   derives a key from an href or a position.

   ⭐ **Progressive enhancement.** With no script the slip names the first unit
   (the renderer wrote that), the progress region and the filter stay hidden,
   and the rail stays open. With no working store the filter still works and
   the progress stays hidden, because the marks are the store's.

   ⚠️ Composed before `read-mark.js`, which stays LAST, and it reads the store
   through the same published name, guarded here because this file has work to
   do without it. */

(function () {
  'use strict';

  var WIDE = '(min-width: 72rem)';
  var STEP = 'aria-current';
  var LISTS = 'nav[aria-label="Contents"] li[id], nav[aria-label="Units"] li[id]';
  var RAIL_UNITS = 'nav[aria-label="Containers"] li[data-unit]';
  var MARKED = 'data-marked';
  /* The hidden words a read row speaks: markup, shown or hidden here
     from the store's answer, so a screen reader hears what the tick shows. */
  var SAID = 'span[data-kind="read-state"]';

  function say(row, read) {
    var words = row.querySelector(SAID);
    if (words) { words.hidden = !read; }
  }

  var store = window.studyforge && window.studyforge.progress;
  var usable = !!(store && store.supported());

  /* --- the rail folds on a narrow screen ---------------------------------- */

  var fold = document.querySelector('nav[aria-label="Containers"] > details');
  if (fold && window.matchMedia) {
    var wide = window.matchMedia(WIDE);
    var place = function () { fold.open = wide.matches; };
    place();
    if (wide.addEventListener) { wide.addEventListener('change', place); }
  }

  /* --- moving the reader on (the brief's §3) ------------------------------ */

  /* ⭐ Marking a unit read brings its Up next slip into view. Focus stays on
     the button: moving it would take the reader somewhere they did not ask to
     go. ⚠️ Here and not in `read-mark.js`, which must never reach for scrolling
     (a mark is an explicit act and nothing about scrolling may infer one). */
  /* --- the rail shows what the reader marked ------------------------------ */

  /* ⭐ On every page that carries a rail, a row whose key the store holds is
     marked, and one it does not hold is cleared — so an unmark shows too.
     ⛔ `data-marked` is set here at read time and emitted by no renderer (R10);
     with no working store the rail shows no marks rather than wrong ones. */
  var railed = [].slice.call(document.querySelectorAll(RAIL_UNITS));
  function paintRail() {
    if (!usable) { return; }
    var held = store.marks();
    railed.forEach(function (row) {
      var holds = held.indexOf(row.getAttribute('data-unit')) !== -1;
      if (holds) { row.setAttribute(MARKED, 'true'); } else { row.removeAttribute(MARKED); }
      say(row, holds);
    });
  }
  paintRail();

  var control = document.querySelector('section[data-section="read-mark"] button');
  var onward = document.querySelector('nav[aria-label="Between units"] a[rel="next"]');
  if (control && railed.length) {
    /* ⚠️ After the turn, for the reason given below: the store's answer is
       written by `read-mark.js`'s listener, which runs after this one. */
    control.addEventListener('click', function () { window.setTimeout(paintRail, 0); });
  }
  if (control && onward && onward.scrollIntoView) {
    /* ⚠️ Read after the turn: `read-mark.js` is composed after this file, so
       its own listener — the one that asks the store and sets `aria-pressed` —
       runs after this one. The state is the store's answer, never the click. */
    control.addEventListener('click', function () {
      window.setTimeout(function () {
        if (control.getAttribute('aria-pressed') !== 'true') { return; }
        var still = window.matchMedia &&
          window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        onward.scrollIntoView({ block: 'nearest', behavior: still ? 'auto' : 'smooth' });
      }, 0);
    });
  }

  var rows = [].slice.call(document.querySelectorAll(LISTS));
  if (!rows.length) { return; }

  var marks = usable ? store.marks() : [];
  rows.forEach(function (row) { say(row, read(row)); });

  function readable(row) { return row.getAttribute('data-readable') === 'true'; }
  function read(row) { return marks.indexOf(row.id) !== -1; }
  function within(root) {
    return rows.filter(function (row) { return root.contains(row) && readable(row); });
  }
  function count(list) { return list.filter(read).length; }
  function fill(holder, value) {
    var slot = holder && holder.querySelector('b, strong');
    if (slot) { slot.textContent = String(value); }
  }

  var units = rows.filter(readable);
  var next = usable ? units.filter(function (row) { return !read(row); })[0] || null : null;
  if (next) { next.setAttribute(STEP, 'step'); }

  /* --- the slip ------------------------------------------------------------ */

  var slip = document.querySelector('nav[aria-label="Up next"]');
  if (slip && usable) {
    var lead = slip.querySelector('a');
    var finished = slip.querySelector('p');
    if (next && lead) {
      var source = next.querySelector('a');
      var title = source ? source.cloneNode(true) : null;
      if (title) {
        [].slice.call(title.querySelectorAll('span')).forEach(function (span) {
          span.parentNode.removeChild(span);
        });
        lead.setAttribute('href', source.getAttribute('href'));
        lead.lastChild.textContent = ' ' + title.textContent.trim();
      }
    } else if (!next && finished && lead && units.length) {
      lead.hidden = true;
      finished.hidden = false;
    }
  }

  /* --- the progress line, the strip, and each group's own count ----------- */

  var region = document.querySelector('section[aria-label="Progress"]');
  if (region && usable) {
    var line = region.querySelector('p');
    var done = count(units);
    fill(line, done);
    fill(line && line.querySelector('span'), units.length - done);
    [].slice.call(region.querySelectorAll('li > a[href^="#"]')).forEach(function (link) {
      var group = document.getElementById(link.getAttribute('href').slice(1));
      if (!group) { return; }
      var members = within(group);
      var share = members.length ? Math.round((100 * count(members)) / members.length) : 0;
      link.style.setProperty('--read', share + '%');
      if (next && group.contains(next)) { link.parentNode.setAttribute(STEP, 'step'); }
      link.addEventListener('click', function () { reveal(group); });
    });
    region.hidden = false;
  }

  [].slice.call(document.querySelectorAll('nav[aria-label="Contents"] summary > small')).forEach(
    function (tally) {
      if (!usable) { return; }
      var members = within(tally.parentNode.parentNode);
      fill(tally, count(members));
      tally.hidden = false;
    }
  );

  /* --- open the group the reader is in ------------------------------------ */

  var groups = [].slice.call(document.querySelectorAll('nav[aria-label="Contents"] details'));

  function reveal(target) {
    for (var node = target; node; node = node.parentNode) {
      if (node.tagName === 'DETAILS') { node.open = true; }
    }
  }

  if (next && groups.length && !window.location.hash) {
    groups.forEach(function (group) { group.open = group.contains(next); });
  }

  /* --- the filter, and expand all / collapse all -------------------------- */

  var search = document.querySelector('form[role="search"]');
  if (!search) { return; }
  var field = search.querySelector('input');
  var status = search.querySelector('[role="status"]');
  var before = null;

  function remember() {
    if (before === null) { before = groups.map(function (group) { return group.open; }); }
  }

  function restore() {
    rows.forEach(function (row) { row.hidden = false; });
    groups.forEach(function (group, at) {
      group.parentNode.hidden = false;
      if (before !== null) { group.open = before[at]; }
    });
    before = null;
    if (status) { status.hidden = true; }
  }

  /* ⚠️ A row's text without its read words: filtering for "read" must not
     match every row the reader finished. */
  function searchable(row) {
    var copy = row.cloneNode(true);
    [].slice.call(copy.querySelectorAll(SAID)).forEach(function (words) {
      words.parentNode.removeChild(words);
    });
    return copy.textContent;
  }

  function narrow(query) {
    var wanted = query.trim().toLowerCase();
    if (!wanted) { restore(); return; }
    remember();
    var shown = 0;
    rows.forEach(function (row) {
      var hit = searchable(row).toLowerCase().indexOf(wanted) !== -1;
      row.hidden = !hit;
      if (hit) { shown += 1; }
    });
    groups.forEach(function (group) {
      var any = rows.some(function (row) { return !row.hidden && group.contains(row); });
      group.parentNode.hidden = !any;
      group.open = any;
    });
    if (status) { fill(status, shown); status.hidden = false; }
  }

  search.addEventListener('submit', function (event) { event.preventDefault(); });
  if (field) {
    field.addEventListener('input', function () { narrow(field.value); });
    field.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') { field.value = ''; restore(); }
    });
  }

  [].slice.call(search.querySelectorAll('button[value]')).forEach(function (button) {
    button.addEventListener('click', function () {
      var opening = button.value === 'expand';
      groups.forEach(function (group) { group.open = opening; });
    });
  });

  search.hidden = false;
}());

/* The mark-as-read control, and the marks surfaced on the two lists.

   ⛔ **The only consumer of the store, and it touches no storage itself.**
   `study-progress.js` owns every read and every write; this file asks it
   questions. Two implementations would be a mark written under one name and
   read back under another, with no symptom but a badge that never lights.

   ⛔ **The store is read through its published name with NO existence guard,
   and that is the ruling rather than an oversight.** The extraction source
   composed its store AFTER the script that read it, guarded the access, and
   shipped a feature that silently never came back with a green suite behind
   it. ⚠️ Here a bundle composed in the wrong order throws on the first line
   that needs the store — loudly, in the console, on the page — and the
   ordering is asserted against the real composition as well. ⛔ This part is
   LAST in `bundle.SCRIPT_PARTS` so that a throw of its own reaches nothing
   else.

   ⛔ **Progressive enhancement, and the control ships HIDDEN.** The region,
   its button and the sentence about where the mark lives are all markup
   (`templates/read-mark.html`) — so nothing here types a word a reader sees
   (R13) — and the region stays hidden until this file has a working store to
   back it. ⚠️ With scripting off, or with site data blocked, the reader is
   shown nothing rather than a control that cannot do anything: a control that
   does nothing is worse than no control.

   ⛔ **Joined by the address and nothing else**. The control carries
   the unit key `Address.unit_key` minted in Python; a row on the root index or
   on a container page carries that same key as its `id`, which is also its
   deep-link anchor. Nothing here composes a key, derives one from a position,
   or reads one back out of an href. */

(function () {
  'use strict';

  /* The first line that needs the store, deliberately unguarded — see above. */
  if (!window.studyforge.progress.supported()) { return; }
  var store = window.studyforge.progress;

  /* The region the unit page carries, and the attribute holding its key. ⚠️
     Spelled here and in `render/page/mark.py`, which is the same two-sided
     spelling every hook on this page has: markup and script cannot import one
     another, and the Python side is the single source for what is EMITTED. */
  var CONTROL = 'section[data-section="read-mark"]';
  var UNIT = 'data-unit';

  /* Whether the reader has marked this. ⚠️ `data-marked` is published in
     `pageassets.SURFACE_HOOKS` because `chrome.css` reaches it; the two values
     below are this file's own and nothing styles them. */
  var MARKED = 'data-marked';
  var STATE = 'data-state';
  var READ = 'read';
  var UNREAD = 'unread';

  /* A row is an `<li>`, checked rather than assumed. ⚠️ A unit page's own
     sections carry a corpus's keys as ids, and a corpus may name one anything
     at all (R1) — so an id that matches a mark is only treated as a row when
     it actually is one. */
  function surface(keys) {
    keys.forEach(function (key) {
      var row = document.getElementById(key);
      if (row && row.tagName === 'LI') { row.setAttribute(MARKED, 'true'); }
    });
  }

  surface(store.marks());

  var control = document.querySelector(CONTROL);
  if (!control) { return; }
  var key = control.getAttribute(UNIT);
  var button = control.querySelector('button');
  if (!key || !button) { return; }
  var labels = [].slice.call(control.querySelectorAll('[' + STATE + ']'));

  /* ⛔ Shown from the STORE's answer, never from what was just pressed: a
     write the browser refused must not leave the page claiming it happened. */
  function show(marked) {
    control.setAttribute(MARKED, marked ? 'true' : 'false');
    button.setAttribute('aria-pressed', marked ? 'true' : 'false');
    labels.forEach(function (label) {
      label.hidden = label.getAttribute(STATE) !== (marked ? READ : UNREAD);
    });
  }

  show(store.marked(key));
  control.hidden = false;

  button.addEventListener('click', function () {
    if (store.marked(key)) { store.unmark(key); } else { store.mark(key); }
    show(store.marked(key));
  });
}());
