"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
var core_1 = require("@angular/core");
var TruncatedText = /** @class */ (function () {
    function TruncatedText() {
        this.ellipsis = '...';
        this.centered = true;
    }
    Object.defineProperty(TruncatedText.prototype, "adjustedText", {
        get: function () {
            if (this.stringWidth(this.text) <= this.width) {
                return this.text;
            }
            var ellipsisWidth = this.stringWidth(this.ellipsis);
            for (var i = this.text.length - 1; i >= 0; i--) {
                var truncatedText = this.text.substring(0, i);
                var widthWithEllipsis = this.stringWidth(truncatedText) + ellipsisWidth;
                if (widthWithEllipsis <= this.width) {
                    this._adjustedText = truncatedText + this.ellipsis;
                    break;
                }
            }
            return this._adjustedText;
        },
        enumerable: true,
        configurable: true
    });
    TruncatedText.prototype.stringWidth = function (str) {
        return str.length * 10;
    };
    __decorate([
        core_1.Input(),
        __metadata("design:type", Object)
    ], TruncatedText.prototype, "position", void 0);
    __decorate([
        core_1.Input(),
        __metadata("design:type", Boolean)
    ], TruncatedText.prototype, "selected", void 0);
    __decorate([
        core_1.Input(),
        __metadata("design:type", Boolean)
    ], TruncatedText.prototype, "valid", void 0);
    __decorate([
        core_1.Input(),
        __metadata("design:type", String)
    ], TruncatedText.prototype, "text", void 0);
    __decorate([
        core_1.Input(),
        __metadata("design:type", Object)
    ], TruncatedText.prototype, "ellipsis", void 0);
    __decorate([
        core_1.Input(),
        __metadata("design:type", Number)
    ], TruncatedText.prototype, "width", void 0);
    __decorate([
        core_1.Input(),
        __metadata("design:type", Object)
    ], TruncatedText.prototype, "centered", void 0);
    TruncatedText = __decorate([
        core_1.Component({
            moduleId: module.id.toString(),
            selector: '[truncated-text]',
            templateUrl: 'truncated-text.component.svg',
            styleUrls: ['truncated-text.component.css']
        })
    ], TruncatedText);
    return TruncatedText;
}());
exports.TruncatedText = TruncatedText;
//# sourceMappingURL=truncated-text.component.js.map